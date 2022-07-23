<template>
  <q-page class="flex" style="flex-flow: column;">
    <div class="q-pl-md q-pr-md q-pt-sm">

      <div class="row justify-center">
        <h3 class="text-center q-mt-none q-mb-md">Test Runs</h3>
      </div>

      <div class="row justify-center">
        <q-table
          :rows="rows"
          :columns="columns"
          :loading="isLoading"
          row-key="name"
          style="width: 100%"
          rows-per-page-options="8"
        >
          <template v-slot:loading>
            <q-inner-loading showing color="primary"></q-inner-loading>
          </template>

          <template v-slot:body="props">
            <q-tr :props="props">
              <q-td key="id" :props="props">
                <q-badge color="green">
                  {{ props.row.id.slice(props.row.id.length - 8) }}
                </q-badge>
              </q-td>
              <q-td key="startTime" :props="props">
                <q-badge v-if="props.row.startTime" color="blue">
                  {{ $filters.formatDate(props.row.startTime) }}
                </q-badge>
              </q-td>
              <q-td key="endTime" :props="props">
                <q-badge v-if="props.row.endTime" color="purple">
                  {{ $filters.formatDate(props.row.endTime) }}
                </q-badge>
              </q-td>
              <q-td key="creator" :props="props">
                <q-badge color="orange">
                  {{ props.row.creator }}
                </q-badge>
              </q-td>
              <q-td key="status" :props="props">
                <q-badge v-if="props.row.status === 'ERROR'" color="red">
                  {{ props.row.status }}
                </q-badge>
                <q-badge v-if="props.row.status === 'FINISHED'" color="green">
                  {{ props.row.status }}
                </q-badge>
                <q-badge v-if="props.row.status === 'QUEUED'" color="orange">
                  {{ props.row.status }}
                </q-badge>
                <q-badge v-if="props.row.status === 'PREPARING'" color="blue">
                  {{ props.row.status }}
                </q-badge>
                <q-badge v-if="props.row.status === 'RUNNING'" color="blue">
                  {{ props.row.status }}
                </q-badge>
                <q-badge v-if="props.row.status === 'UNKNOWN'" color="gray">
                  {{ props.row.status }}
                </q-badge>
              </q-td>
              <q-td key="creator" :props="props">
                <q-btn round color="primary" text-color="white" icon="add" padding="xs"></q-btn>
              </q-td>
            </q-tr>
          </template>
        </q-table>
      </div>

    </div>
  </q-page>
</template>

<script>

import api from "src/composables/touchstone-api-v1";
import { computed, ref } from "vue"

export default {
  name: "Runs",

  setup() {
    const columns = [
      { name: 'id', required: true, label: 'ID', align: 'left', field: 'id' },
      { name: 'startTime', align: 'left', label: 'Start Time', field: 'startTime', sortable: true },
      { name: 'endTime', align: 'left', label: 'End Time', field: 'endTime', sortable: true },
      { name: 'creator', align: 'left', label: 'Creator', field: 'creator' },
      { name: 'status', align: 'left', label: 'Status', field: 'status' },
    ]

    const rows = ref();
    api.getTestRuns().then(res => { rows.value = res.data.data; isLoading.value = false; });

    const isLoading = ref(true)

    return {
      columns,
      rows,
      isLoading,
      pagesNumber: computed(() => {
        return Math.ceil(rows.value.length / 8)
      })
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
