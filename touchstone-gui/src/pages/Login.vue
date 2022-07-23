<template>
  <q-page class="flex flex-center">
    <div class="q-pa-md">

      <div class="row justify-center">
        <h3 class="text-center q-mt-none q-mb-lg-lg">Welcome to Touchstone</h3>
      </div>

      <div class="row justify-center">
        <div class="col col-8">

          <q-card square bordered>
            <q-card-section class="q-pb-xs">
              <h6 class="text-center q-ma-none" style="opacity:0.5">Please log in to your organisation</h6>
            </q-card-section>

            <q-card-section>
              <q-form class="q-pl-sm q-pr-sm" @submit="onSubmit()">

                <q-input
                  v-model=form.address
                  class="q-pb-lg"
                  dense
                  filled
                  outlined
                  label="Server Address"
                  :error="$v.address.$error"
                  :error-message="$v.address.$errors.length > 0 ? $v.address.$errors[0].$message : ''"
                  @blur="$v.address.$touch()"
                >
                  <template #prepend>
                    <q-icon name="computer"></q-icon>
                  </template>
                </q-input>

                <q-input
                  v-model=form.username
                  class="q-pb-lg"
                  dense
                  filled
                  outlined
                  label="Username"
                  :error="$v.username.$error"
                  :error-message="$v.username.$errors.length > 0 ? $v.username.$errors[0].$message : ''"
                  @blur="$v.username.$touch()"
                >
                  <template #prepend>
                    <q-icon name="person"></q-icon>
                  </template>
                </q-input>

                <q-input
                  v-model=form.password
                  class="q-pb-lg"
                  dense
                  filled
                  outlined
                  label="Password"
                  type="password"
                  :error="$v.password.$error"
                  :error-message="$v.password.$errors.length > 0 ? $v.password.$errors[0].$message : ''"
                  @blur="$v.password.$touch()"
                >
                  <template #prepend>
                    <q-icon name="password"></q-icon>
                  </template>
                </q-input>

                <q-btn class="full-width q-pb-sm" label="Login" type="submit" color="primary" :loading="isSubmitting"></q-btn>
              </q-form>
            </q-card-section>
          </q-card>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script>
import api from "src/composables/touchstone-api-v1";
import router from "src/router"
import { createErrorToast } from "boot/toast";
import { reactive, ref } from "vue"
import { useVuelidate } from '@vuelidate/core'
import { required } from '@vuelidate/validators'

export default {
  name: "Login",

  setup() {
    const form = reactive({
      address: "",
      username: "",
      password: ""
    })

    const isSubmitting = ref(false)

    const rules = {
      address: { required },
      username: { required },
      password: { required }
    }

    const $v = useVuelidate(rules, form)

    function onSubmit() {
      this.$v.$touch()
      if (this.$v.$errors.length === 0)
      {
        isSubmitting.value = true
        setTimeout(() => {
          api.address = form.address
          api.logIn(form.username, form.password)
            .then(() => {
              router().push({ name: "overview" })
            })
            .catch(err => {
              createErrorToast(err)
            })
            .finally( isSubmitting.value = false )
        }, 500);
      }
    }

    return {
      form, isSubmitting, onSubmit, $v
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
