/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  root: true,
  'extends': [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier/skip-formatting'
  ],
  parserOptions: {
    ecmaVersion: 'latest'
  },
  rules: {
    // 允许 v-model 带参数（Element Plus 分页组件需要）
    'vue/no-v-model-argument': 'off',
    // 允许使用 v-html
    'vue/no-v-html': 'off',
    // 允许使用 console
    'no-console': 'off',
    // 允许使用 debugger
    'no-debugger': 'off'
  }
}
