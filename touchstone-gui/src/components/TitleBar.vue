<template>
  <q-bar class="bg-black q-electron-drag text-white">
    <q-space></q-space>

    <q-btn class="titlebar-button minimize" dense flat icon="minimize" @click="minimize"></q-btn>
<!--    <q-btn class="titlebar-button maximize" @click="maximize" dense flat icon="crop_square"></q-btn>-->
    <q-btn class="titlebar-button close" dense flat icon="close" @click="close"></q-btn>
  </q-bar>
</template>

<script>
import {useQuasar} from 'quasar'

export default {
  name: "TitleBar",

  setup() {
    const $q = useQuasar()

    function minimize() {
      if (process.env.MODE === 'electron') {
        $q.electron.remote.BrowserWindow.getFocusedWindow().minimize()
      }
    }

    function maximize() {
      if (process.env.MODE === 'electron') {
        const win = $q.electron.remote.BrowserWindow.getFocusedWindow()

        if (win.isMaximized()) {
          win.unmaximize()
        } else {
          win.maximize()
        }
      }
    }

    function close() {
      if (process.env.MODE === 'electron') {
        $q.electron.remote.BrowserWindow.getFocusedWindow().close()
      }
    }

    return {
      minimize, maximize, close
    }
  }
}
</script>

<style lang="scss" scoped>

.titlebar-button {

  color: #7f8c8d;

  &:hover {
    &.minimize {
      color: #3498db;
    }

    &.maximize {
      color: #2ecc71;
    }

    &.close {
      color: #e74c3c;
    }
  }
}

</style>
