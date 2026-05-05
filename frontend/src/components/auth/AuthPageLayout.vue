<template>
  <div class="auth-shell">
    <div :class="layoutClasses">
      <div class="auth-page__visual" aria-hidden="true">
        <div class="auth-page__visual-brand">
          <span class="auth-page__visual-mark">
            <el-icon><Reading /></el-icon>
          </span>
          <div class="auth-page__visual-brand-copy">
            <strong>学习辅助系统</strong>
            <span>Learning Assistant Platform</span>
          </div>
        </div>

        <div class="auth-page__visual-copy">
          <h3 class="auth-page__visual-title">{{ visualTitle }}</h3>
          <p class="auth-page__visual-desc">{{ visualDescription }}</p>
        </div>

        <div class="auth-page__illustration-shell">
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

        <div class="auth-page__visual-tags">
          <span v-for="item in visualTags" :key="item" class="auth-page__visual-tag">
            {{ item }}
          </span>
        </div>
      </div>

      <div :class="panelClasses">
        <div class="auth-page__panel-shell">
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

const visualTitle = computed(() =>
  props.visualType === 'register'
    ? '加入班级，开启有序的学习协同体验'
    : '让教学更高效，让学习更清晰'
)

const visualDescription = computed(() =>
  props.visualType === 'register'
    ? '通过邀请码关联班级，在同一平台查看课程、提交作业、获取学习反馈。'
    : '一站式课程、作业、成绩管理平台，为师生提供全流程教学协同体验。'
)

const visualTags = computed(() => ['课程管理', '作业协同', '成绩反馈'])
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  padding: 32px 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 0% 0%, rgba(88, 146, 255, 0.18), transparent 28%),
    radial-gradient(circle at 100% 10%, rgba(90, 177, 255, 0.14), transparent 24%),
    linear-gradient(180deg, #f7fbff 0%, #edf4ff 48%, #f5f9ff 100%);
}

.auth-page {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(420px, 1fr);
  align-items: center;
  gap: 26px;
  width: min(100%, 1180px);
}

.auth-page--wide {
  width: min(100%, 1220px);
  grid-template-columns: minmax(360px, 0.95fr) minmax(440px, 1.05fr);
}

.auth-page__visual {
  position: relative;
  min-height: 720px;
  padding: 42px 36px;
  border-radius: 34px;
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 18%, rgba(255, 255, 255, 0.2), transparent 26%),
    radial-gradient(circle at 88% 78%, rgba(121, 188, 255, 0.24), transparent 24%),
    linear-gradient(160deg, #165dff 0%, #2c76ff 46%, #0e42b3 100%);
  box-shadow: 0 30px 60px rgba(22, 93, 255, 0.2);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.auth-page--register .auth-page__visual {
  min-height: 760px;
}

.auth-page__visual::before {
  content: '';
  position: absolute;
  inset: 24px;
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0));
  pointer-events: none;
}

.auth-page__visual-copy,
.auth-page__illustration-shell {
  position: relative;
  z-index: 1;
}

.auth-page__visual-brand {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.auth-page__visual-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.auth-page__visual-brand-copy {
  display: flex;
  flex-direction: column;
}

.auth-page__visual-brand-copy strong {
  font-size: 20px;
  line-height: 1.2;
  color: #fff;
}

.auth-page__visual-brand-copy span {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(236, 244, 255, 0.72);
}

.auth-page__visual-title {
  margin: 26px 0 16px;
  max-width: 460px;
  font-size: 42px;
  line-height: 1.18;
  color: #fff;
}

.auth-page__visual-desc {
  max-width: 420px;
  margin: 0;
  font-size: 16px;
  line-height: 1.9;
  color: rgba(236, 244, 255, 0.88);
}

.auth-page__visual-tags {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.auth-page__visual-tag {
  min-height: 38px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.auth-page__illustration-shell {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-page__illustration {
  position: relative;
  width: 320px;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10px);
}

.auth-page__svg {
  width: 100%;
  height: 100%;
}

.auth-page__panel {
  min-height: 720px;
  border-radius: 34px;
  border: 1px solid rgba(105, 150, 232, 0.14);
  background: #ffffff;
  box-shadow: 0 28px 58px rgba(42, 82, 152, 0.1);
  display: flex;
  align-items: center;
}

.auth-panel--wide {
  min-height: 760px;
}

.auth-page__panel-shell {
  width: min(100%, 460px);
  margin: 0 auto;
  padding: 52px 34px;
}

.auth-page__brand {
  margin-bottom: 30px;
}

.auth-page__brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #165dff 0%, #4f8cff 100%);
  box-shadow: 0 14px 26px rgba(22, 93, 255, 0.2);
}

.auth-page__brand :deep(.auth-brand__title) {
  margin: 0;
  font-size: 32px;
  line-height: 1.18;
  color: #1f2f49;
}

.auth-page__brand :deep(.auth-brand__subtitle) {
  margin: 10px 0 0;
  font-size: 15px;
  line-height: 1.85;
  color: #667892;
}

.auth-page__form {
  margin-top: 10px;
}

.auth-page__note {
  margin-top: 28px;
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f9fbff 0%, #f2f7ff 100%);
  border: 1px solid rgba(111, 152, 226, 0.14);
}

.auth-page__linkbar {
  margin-top: 24px;
  text-align: center;
}

.auth-page__note :deep(p) {
  margin: 0;
  line-height: 1.8;
  color: #6b7c96;
}

.auth-page__note :deep(p + p) {
  margin-top: 8px;
}

.auth-page__form :deep(.auth-form .el-form-item) {
  margin-bottom: 20px;
}

.auth-page__form :deep(.auth-form .el-input__wrapper) {
  min-height: 50px;
  border-radius: 16px;
  background: #f9fbff;
  box-shadow: 0 0 0 1px rgba(111, 152, 226, 0.12);
  transition: box-shadow 0.22s ease, background-color 0.22s ease, transform 0.22s ease;
}

.auth-page__form :deep(.auth-form .el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(22, 93, 255, 0.32), 0 12px 24px rgba(22, 93, 255, 0.08);
}

.auth-page__form :deep(.auth-form .el-input__wrapper:hover) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(22, 93, 255, 0.22);
}

.auth-page__form :deep(.auth-form .el-input__prefix),
.auth-page__form :deep(.auth-form .el-input__suffix),
.auth-page__form :deep(.auth-form .el-input__icon) {
  color: #86909c;
}

.auth-page__form :deep(.auth-submit-button) {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #165dff 0%, #4f8cff 100%);
  box-shadow: 0 16px 28px rgba(22, 93, 255, 0.2);
  transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
}

.auth-page__form :deep(.auth-submit-button:hover),
.auth-page__form :deep(.auth-submit-button:focus) {
  filter: brightness(0.97);
  transform: translateY(-2px);
  box-shadow: 0 20px 30px rgba(22, 93, 255, 0.24);
}

.auth-page__form :deep(.auth-submit-button.is-loading:hover),
.auth-page__form :deep(.auth-submit-button.is-loading:focus) {
  transform: none;
  box-shadow: 0 16px 28px rgba(22, 93, 255, 0.2);
}

.auth-page__linkbar :deep(a) {
  margin-left: 6px;
  color: #165dff;
  font-weight: 600;
  text-decoration: none;
}

.auth-page__linkbar :deep(a:hover) {
  color: #0e42b3;
}

@media (hover: hover) {
  .auth-page__visual,
  .auth-page__panel,
  .auth-page__illustration {
    transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
  }

  .auth-page__visual:hover {
    box-shadow: 0 36px 72px rgba(22, 93, 255, 0.24);
  }

  .auth-page__panel:hover {
    box-shadow: 0 32px 64px rgba(42, 82, 152, 0.12);
  }

  .auth-page__visual:hover .auth-page__illustration {
    transform: translateY(-4px);
    box-shadow: 0 24px 40px rgba(14, 42, 107, 0.18);
  }
}

@media (max-width: 992px) {
  .auth-page,
  .auth-page--wide {
    grid-template-columns: 1fr;
    gap: 20px;
    max-width: 760px;
  }

  .auth-page__visual,
  .auth-page--register .auth-page__visual {
    min-height: 420px;
  }

  .auth-page__panel,
  .auth-panel--wide {
    min-height: auto;
  }

  .auth-page__panel-shell {
    width: min(100%, 520px);
    padding: 40px 28px;
  }

  .auth-page__illustration-shell {
    min-height: 240px;
  }

  .auth-page__illustration {
    width: 260px;
    height: 260px;
  }
}

@media (max-width: 768px) {
  .auth-shell {
    padding: 20px 16px;
  }

  .auth-page__visual {
    min-height: 360px;
    padding: 28px 22px;
    border-radius: 26px;
  }

  .auth-page__visual::before {
    inset: 16px;
    border-radius: 20px;
  }

  .auth-page__visual-title {
    font-size: 30px;
  }

  .auth-page__panel,
  .auth-panel--wide {
    border-radius: 26px;
  }

  .auth-page__panel-shell {
    padding: 32px 22px;
  }

  .auth-page__illustration {
    width: 220px;
    height: 220px;
  }
}

@media (max-width: 480px) {
  .auth-shell {
    padding: 14px 12px;
  }

  .auth-page__visual {
    min-height: 320px;
    border-radius: 22px;
  }

  .auth-page__visual-title {
    font-size: 26px;
  }

  .auth-page__visual-desc,
  .auth-page__brand :deep(.auth-brand__subtitle) {
    font-size: 14px;
  }

  .auth-page__panel,
  .auth-panel--wide {
    border-radius: 22px;
  }

  .auth-page__panel-shell {
    padding: 28px 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-page__visual,
  .auth-page__panel,
  .auth-page__visual-card,
  .auth-page__illustration,
  .auth-page__form :deep(.auth-form .el-input__wrapper),
  .auth-page__form :deep(.auth-submit-button) {
    transition: none;
  }
}
</style>
