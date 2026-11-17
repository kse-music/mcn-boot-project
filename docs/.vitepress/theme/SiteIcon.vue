<script setup lang="ts">
defineProps<{ name: 'arrow' | 'external' | 'code' | 'layers' | 'sliders' | 'book' }>()

const paths = {
  arrow: 'M4 12h15m-6-6 6 6-6 6',
  external: 'M14 4h6v6M20 4l-10 10M10 4H4v16h16v-6',
  code: 'm8 7-5 5 5 5m8-10 5 5-5 5m-3-14-2 18',
  layers: 'm12 3 10 5-10 5L2 8l10-5Zm-10 9 10 5 10-5M2 16l10 5 10-5',
  sliders: 'M4 4v6m0 4v6M12 4v10m0 4v2M20 4v2m0 4v10M1 10h6m2 4h6m2-8h6',
  book: 'M12 6v15M12 6C9 3 4 3 2 4v15c4-1 7-1 10 2 3-3 6-3 10-2V4c-3-1-7-1-10 2Z',
}
</script>

<template>
  <svg class="mcn-icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <path :d="paths[name]" />
  </svg>
</template>
