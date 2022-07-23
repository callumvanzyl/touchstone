import axios from 'axios'
import { reactive } from 'vue'

class TouchstoneApiV1 {

  constructor() {
    this.http = null;

    this.authenticatedUsername = null
    this.isAuthenticated = false;
  }

  set address(address) {
    this.http = axios.create({ baseURL: address })
  }

  logIn(username, password) {
    return this.http.post("touchstone/api/v1/login", {username: username, password: password})
      .then(res => {
        this.http.defaults.headers.common["Authorization"] = res.data.data
        this.authenticatedUsername = username
        this.isAuthenticated = true
        return Promise.resolve()
      })
      .catch(err => {
        this.logOut()
        if (err.response) {
          if (err.response.status === 404) {
            return Promise.reject("The server does not exist or is offline")
          }
          else if (err.response.status === 401) {
            return Promise.reject(err.response.data.reason)
          }
        }
        return Promise.reject("An error occurred, please try again later")
      })
  }

  logOut() {
    this.http.defaults.headers.common["Authorization"] = null
    this.authenticatedUsername = null
    this.isAuthenticated = false
  }

  getTestRuns() {
    return this.http.get("touchstone/api/v1/runner")
      .then(res => {
        return Promise.resolve(res)
      })
      .catch(err => {
        if (err.response) {
          return Promise.reject(err.response.data.reason)
        }
        return Promise.reject("An error occurred, please try again later")
      })
  }

}

const api = reactive(new TouchstoneApiV1())

export default api
