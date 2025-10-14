import { reactive, readonly } from 'vue'

type ModalVariant = 'default' | 'danger' | 'success'
type ModalKind = 'confirm' | 'alert'

interface ModalState {
  open: boolean
  kind: ModalKind
  title: string
  html?: string
  confirmText?: string
  cancelText?: string
  variant: ModalVariant
  resolve?: (v: boolean) => void
}

const state = reactive<ModalState>({
  open: false,
  kind: 'confirm',
  title: '',
  html: '',
  confirmText: 'Confirm',
  cancelText: 'Cancel',
  variant: 'default',
  resolve: undefined
})

function confirm(opts: Partial<Omit<ModalState,'open'|'resolve'|'kind'>> & { title: string; html?: string }) {
  return new Promise<boolean>((resolve) => {
    Object.assign(state, {
      open: true,
      kind: 'confirm',
      title: opts.title,
      html: opts.html ?? '',
      confirmText: opts.confirmText ?? 'Confirm',
      cancelText: opts.cancelText ?? 'Cancel',
      variant: opts.variant ?? 'default',
      resolve
    })
  })
}

function alert(opts: Partial<Omit<ModalState,'open'|'resolve'|'kind'>> & { title: string; html?: string }) {
  return new Promise<boolean>((resolve) => {
    Object.assign(state, {
      open: true,
      kind: 'alert',
      title: opts.title,
      html: opts.html ?? '',
      confirmText: opts.confirmText ?? 'OK',
      cancelText: undefined,
      variant: opts.variant ?? 'default',
      resolve
    })
  })
}

function close(result: boolean) {
  state.open = false
  const r = state.resolve
  state.resolve = undefined
  r?.(result)
}

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.provide('modal', { state: readonly(state), confirm, alert, close })
})
