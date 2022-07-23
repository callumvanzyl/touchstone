import subprocess, sys, requests, time

TOUCHSTONE_URL = "http://localhost:8080"
TOUCHSTONE_USERNAME = "admin"
TOUCHSTONE_PASSWORD = "admin"
TOUCHSTONE_SCHEMATIC_ID = "6024a21c6a1acf3e3866dedf"  # You can create a new schematic by POSTing to /touchstone/api/v1/schematics

DOCKER_FILE_PATH = "./touchstone"
DOCKER_IMAGE_TAG = "touchstone_app:cicd"  # Make sure you use this image tag in your deployment plan
DOCKER_IMAGE_BUILD_ARGS = {"CHROME_WEB_DRIVER_PATH": "/usr/bin/chromedriver", "SERVER_PORT": "8080"}

# Touchstone should be connected to the same registry, leave these fields blank if you are using local images
DOCKER_REGISTRY_URL = ""
DOCKER_REGISTRY_USERNAME = ""
DOCKER_REGISTRY_PASSWORD = ""


def main():
    print("RUNNING TOUCHSTONE CI/CD")
    print("---------------------------------")
    # BUILD IMAGE
    print(f"Building Docker image {DOCKER_IMAGE_TAG}, this could take a while!")

    parsed_args = ""
    for k, v in DOCKER_IMAGE_BUILD_ARGS.items():
        parsed_args += f"--build-arg {k}={v} "

    subprocess.run(
        f"docker build {parsed_args}-t {DOCKER_IMAGE_TAG} {DOCKER_FILE_PATH}",
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL
    )
    print(f"Built Docker image {DOCKER_IMAGE_TAG}.")

    if DOCKER_REGISTRY_URL != "":
        subprocess.run(
            f"docker login --username={DOCKER_REGISTRY_USERNAME} --password={DOCKER_REGISTRY_PASSWORD}",
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        subprocess.run(
            f"docker push {DOCKER_IMAGE_TAG}",
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        print(f"Pushed Docker image {DOCKER_IMAGE_TAG} to registry {DOCKER_REGISTRY_URL}.")

    try:
        auth_req = requests.post(
            url=f"{TOUCHSTONE_URL}/touchstone/api/v1/login",
            json={"username": TOUCHSTONE_USERNAME, "password": TOUCHSTONE_PASSWORD}
        )
    except requests.exceptions.ConnectionError:
        print("ABORTING: Could not connect to Touchstone, is it online and reachable?")
        sys.exit(1)

    if auth_req.status_code == 401:
        print("ABORTING: Touchstone failed to authenticate, make sure you provided a valid username and password.")
        sys.exit(1)
    else:
        print("Authenticated with Touchstone successfully.")

    auth_header = {"Authorization": auth_req.json()["data"]}

    run_req = requests.post(
        url=f"{TOUCHSTONE_URL}/touchstone/api/v1/runner/queue",
        json={"schematicId": TOUCHSTONE_SCHEMATIC_ID},
        headers=auth_header
    )

    if run_req.status_code != 201:
        print(f"ABORTING: Something went wrong when sending the run request ({run_req.json()['reason']}).")
        sys.exit(1)

    print(f"Sent run request {run_req.json()['data']['id']}, please wait for Touchstone to finish.")

    global done
    done = False

    while not done:
        run_res = requests.get(
            url=f"{TOUCHSTONE_URL}/touchstone/api/v1/runner/{run_req.json()['data']['id']}",
            headers=auth_header
        )

        run_status = run_res.json()["data"]["status"]

        if run_status == "FINISHED":
            done = True
            passed = run_res.json()["data"]["stepsFailed"] == "0"
            if passed:
                print("The test run PASSED!")
            else:
                print("The test run FAILED!")

            print(f"Queued time: {run_res.json()['data']['queuedTime']}")
            print(f"Start time: {run_res.json()['data']['startTime']}")
            print(f"End time: {run_res.json()['data']['endTime']}")
            print(f"Steps passed: {run_res.json()['data']['stepsPassed']}")
            print(f"Steps failed: {run_res.json()['data']['stepsFailed']}")
            print(f"For more information make an authorised request to {TOUCHSTONE_URL}/touchstone/api/v1/resources/report/{run_res.json()['data']['id']}")

            if passed:
                sys.exit(0)
            else:
                sys.exit(1)
        else:
            time.sleep(1)

    sys.exit(1)


if __name__ == "__main__":
    main()