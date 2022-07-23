
const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/Login.vue'), name: "login" },

      { path: '', component: () => import('pages/Overview.vue'), name: "overview" },
      { path: '', component: () => import('pages/Runs.vue'), name: "runs" }
    ]
  }
]

export default routes
