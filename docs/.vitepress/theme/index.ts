import DefaultTheme from 'vitepress/theme-without-fonts'
import type { Theme } from 'vitepress'
import Layout from './Layout.vue'
import './styles.css'

export default {
  extends: DefaultTheme,
  Layout,
} satisfies Theme
