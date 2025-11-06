// 扩展Vue Router的meta类型
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
  }
}

export {};