<template>
  <router-view/>
</template>

<script setup lang="ts">
import AuthView from './components/AuthView.vue'
</script>

<style>
/* 重置全局样式 */
body, html {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}
</style>
