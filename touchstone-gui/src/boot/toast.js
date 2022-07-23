import { Notify } from 'quasar'

function createErrorToast(message) {
  Notify.create({
    type: "negative",
    timeout: 3000,
    message: message,
    position: "top"
  })
}

function createSuccessToast(message) {
  Notify.create({
    type: "positive",
    timeout: 3000,
    message: message,
    position: "top"
  })
}

export { createErrorToast, createSuccessToast }
