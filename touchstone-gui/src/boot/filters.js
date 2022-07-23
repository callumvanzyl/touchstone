import moment from "moment"

export default ({ app }) => {
  app.config.globalProperties.$filters = {
    formatDate(date) {
      if (date != null) {
        return moment(date).format("YYYY/MM/DD HH:mm:ss");
      } else {
        return "N/A"
      }
    }
  }
}
