// types/modal.d.ts

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
}

interface ConfirmOptions {
  title: string
  html?: string
  confirmText?: string
  cancelText?: string
  variant?: ModalVariant
}

interface AlertOptions {
  title: string
  html?: string
  confirmText?: string
  variant?: ModalVariant
}

declare module '#app' {
  interface NuxtApp {
    $modal: {
      state: Readonly<ModalState>
      confirm: (opts: ConfirmOptions) => Promise<boolean>
      alert: (opts: AlertOptions) => Promise<boolean>
      close: (result: boolean) => void
    }
  }
}

declare module 'vue' {
  interface ComponentCustomProperties {
    $modal: {
      state: Readonly<ModalState>
      confirm: (opts: ConfirmOptions) => Promise<boolean>
      alert: (opts: AlertOptions) => Promise<boolean>
      close: (result: boolean) => void
    }
  }
}

export {}
