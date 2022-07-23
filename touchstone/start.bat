@echo off
for /F "tokens=*" %%A in  (env/.env, env/.env.dev) do SET %%A
docker-compose build --build-arg SERVER_PORT=%SERVER_PORT% --build-arg CHROME_WEB_DRIVER_PATH=%CHROME_WEB_DRIVER_PATH%
docker-compose up -d
@echo on