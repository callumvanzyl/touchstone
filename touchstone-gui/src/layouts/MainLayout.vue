<template>
  <q-layout view="hHh Lpr lff">
    <q-header elevated>
      <TitleBar></TitleBar>
    </q-header>

    <q-drawer
      show-if-above
      :width="200"
      :breakpoint="500"
      bordered
      class="bg-grey-3"
    >
      <q-scroll-area class="fit">
        <q-list>

          <q-item>
            <q-item-section avatar>
              <q-icon name="person"></q-icon>
            </q-item-section>
            <q-item-section>
              {{ authenticatedUsername || "Guest" }}
            </q-item-section>
          </q-item>
          <q-separator></q-separator>

          <template v-for="(destination, index) in destinations" :key="index">
            <q-item
              v-if="isAuthenticated"
              clickable
              active-class="active-destination"
              :to="{ name: destination.route }"
              @click="activeDestination = destination.label"
            >
              <q-item-section avatar>
                <q-icon :name="destination.icon"></q-icon>
              </q-item-section>
              <q-item-section>
                {{ destination.label }}
              </q-item-section>
            </q-item>
            <q-separator v-if="destination.separator" :key="'sep' + index" ></q-separator>
          </template>

        </q-list>

        <div class="row justify-center">
          <q-btn v-if="isAuthenticated" style="width: 70%" class="q-mt-md" label="Log Out" color="primary" @click="onLogOut"></q-btn>
        </div>

      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import TitleBar from "components/TitleBar.vue";
import api from "src/composables/touchstone-api-v1";
import router from "src/router";
import { computed, defineComponent, ref } from "vue"

export default defineComponent({
  name: 'MainLayout',

  components: {
    TitleBar
  },

  setup () {
    const destinations = [
      {
        icon: "public",
        label: "Overview",
        route: "overview",
        separator: false
      },
      {
        icon: "quiz",
        label: "Test Runs",
        route: "runs",
        separator: true
      }
    ]

    function onLogOut() {
      api.address = null
      api.logOut()
      router().push({ name: "login" })
    }

    return {
      activeDestination: ref(""),
      authenticatedUsername: computed(() => api.authenticatedUsername),
      isAuthenticated: computed(() => api.isAuthenticated),
      destinations,
      onLogOut
    }
  }
})
</script>

<style lang="scss" scoped>

.active-destination {
  color: white;
  background: $primary;
}

</style>
