<template>
  <div class="auth-shell">
    <div :class="layoutClasses">
      <div class="auth-page__visual" aria-hidden="true">
        <div class="auth-page__illustration">
          <svg
            v-if="visualType === 'register'"
            viewBox="0 0 320 320"
            class="auth-page__svg"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="82" y="182" width="156" height="16" rx="8" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M112 182V138C112 130.268 118.268 124 126 124H194C201.732 124 208 130.268 208 138V182" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <rect x="122" y="136" width="76" height="44" rx="10" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M160 92V116" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M142 102H178" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <circle cx="64" cy="116" r="26" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M64 104V128" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M52 116H76" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <rect x="238" y="102" width="42" height="78" rx="10" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M248 120H270" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M248 136H270" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M248 152H264" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M88 230C110 212 136 204 160 204C184 204 210 212 232 230" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M114 72L138 52" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M206 72L182 52" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
          </svg>
          <svg
            v-else
            viewBox="0 0 320 320"
            class="auth-page__svg"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="78" y="182" width="164" height="16" rx="8" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <rect x="98" y="122" width="124" height="76" rx="14" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M124 220H198" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M145 198V220" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M177 198V220" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <rect x="40" y="104" width="54" height="88" rx="10" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M52 120H82" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M52 136H82" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M52 152H74" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M246 112C246 95.4315 259.431 82 276 82C292.569 82 306 95.4315 306 112C306 124.737 298.056 135.617 286.848 139.947V153H265.152V139.947C253.944 135.617 246 124.737 246 112Z" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <path d="M268 168H284" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M268 180H284" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M82 240C102 220 132 210 160 210C188 210 218 220 238 240" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M82 94L122 54" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M238 94L198 54" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
            <path d="M146 46H174" stroke="rgba(255,255,255,0.2)" stroke-width="3" stroke-linecap="round" />
          </svg>
        </div>
      </div>

      <div :class="panelClasses">
        <div class="auth-brand auth-page__brand">
          <span class="auth-brand__mark auth-page__brand-mark">
            <el-icon><Reading /></el-icon>
          </span>
          <div>
            <h2 class="auth-brand__title">{{ title }}</h2>
            <p class="auth-brand__subtitle">{{ subtitle }}</p>
          </div>
        </div>

        <div class="auth-page__form">
          <slot />
        </div>

        <div v-if="$slots.note" class="auth-note auth-page__note">
          <div v-if="noteTitle" class="auth-note__title">{{ noteTitle }}</div>
          <slot name="note" />
        </div>

        <div v-if="linkText && linkLabel" class="auth-linkbar auth-page__linkbar">
          {{ linkText }}<router-link :to="linkTo">{{ linkLabel }}</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Reading } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  title: string
  subtitle: string
  noteTitle?: string
  linkText?: string
  linkLabel?: string
  linkTo?: string
  visualType?: 'login' | 'register'
  wide?: boolean
}>(), {
  noteTitle: '',
  linkText: '',
  linkLabel: '',
  linkTo: '/',
  visualType: 'login',
  wide: false
})

const layoutClasses = computed(() => [
  'auth-page',
  `auth-page--${props.visualType}`,
  { 'auth-page--wide': props.wide }
])

const panelClasses = computed(() => [
  'auth-panel',
  'auth-page__panel',
  { 'auth-panel--wide': props.wide }
])
</script>

<style scoped>
.auth-page {
  display: grid;
  grid-template-columns: minmax(320px, 460px) minmax(360px, 440px);
  align-items: center;
  gap: 36px;
  width: min(100%, 1036px);
}

.auth-page--wide {
  width: min(100%, 1064px);
  grid-template-columns: minmax(320px, 460px) minmax(360px, 468px);
}

.auth-page__visual {
  position: relative;
  min-height: 560px;
  border-radius: 16px;
  padding: 40px 36px;
  overflow: hidden;
  background: linear-gradient(180deg, #165dff 0%, #0e42b3 100%);
  box-shadow: 0 12px 32px rgba(22, 93, 255, 0.18);
}

.auth-page--register .auth-page__visual {
  min-height: 640px;
}

.auth-page__visual::before {
  content: '';
  position: absolute;
  inset: 28px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0));
  pointer-events: none;
}

.auth-page__illustration {
  position: absolute;
  inset: 50% auto auto 50%;
  width: 300px;
  height: 300px;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-page__svg {
  width: 100%;
  height: 100%;
}

.auth-page__panel {
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
}

.auth-page__brand {
  margin-bottom: 24px;
}

.auth-page__brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #165dff;
  box-shadow: none;
}

.auth-page__form {
  margin-top: 8px;
}

.auth-page__note {
  margin-top: 24px;
}

.auth-page__linkbar {
  margin-top: 24px;
}

.auth-page__note :deep(p) {
  margin: 0;
}

.auth-page__note :deep(p + p) {
  margin-top: 6px;
}

.auth-page__form :deep(.auth-form .el-input__wrapper:hover) {
  border-color: #165dff;
  background: #ffffff;
}

.auth-page__form :deep(.auth-form .el-input__prefix),
.auth-page__form :deep(.auth-form .el-input__suffix),
.auth-page__form :deep(.auth-form .el-input__icon) {
  color: #86909c;
}

.auth-page__form :deep(.auth-submit-button) {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: #165dff;
  border-color: #165dff;
  transition: all 0.2s ease;
}

.auth-page__form :deep(.auth-submit-button:hover),
.auth-page__form :deep(.auth-submit-button:focus) {
  background: #0e42b3;
  border-color: #0e42b3;
  transform: translateY(-1px);
}

.auth-page__form :deep(.auth-submit-button.is-loading:hover),
.auth-page__form :deep(.auth-submit-button.is-loading:focus) {
  transform: none;
}

@media (max-width: 992px) {
  .auth-page,
  .auth-page--wide {
    grid-template-columns: 1fr;
    gap: 20px;
    max-width: 548px;
  }

  .auth-page__visual,
  .auth-page--register .auth-page__visual {
    min-height: 320px;
  }

  .auth-page__illustration {
    width: 240px;
    height: 240px;
  }
}

@media (max-width: 768px) {
  .auth-page__visual {
    display: none;
  }

  .auth-page,
  .auth-page--wide {
    grid-template-columns: 1fr;
  }
}
</style>
