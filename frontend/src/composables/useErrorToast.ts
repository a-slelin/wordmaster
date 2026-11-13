import { useI18n } from 'vue-i18n'
import { ApiError } from '@/api'
import { useUiStore } from '@/stores/ui'

/** Statuses with a dedicated localized message (the server answers in English only). */
const STATUS_KEYS: Record<number, string> = {
  0: 'common.networkError',
  403: 'errors.forbidden',
  404: 'errors.notFound',
  409: 'errors.conflict',
  429: 'errors.tooManyRequests',
  500: 'errors.server',
}

/** Shows a human-friendly toast for an API error. */
export function useErrorToast() {
  const ui = useUiStore()
  const { t } = useI18n()

  return (error: unknown) => {
    if (error instanceof ApiError) {
      // 401 is handled globally: the session is refreshed or the user is sent to the login page.
      if (error.status === 401) return

      const key = STATUS_KEYS[error.status] ?? (error.status > 500 ? STATUS_KEYS[500] : undefined)
      if (key) {
        ui.toast(t(key), 'error')
        return
      }
      const fields = Object.values(error.fieldErrors)
      ui.toast(fields[0] ?? error.message ?? t('common.somethingWrong'), 'error')
      return
    }
    ui.toast(t('common.somethingWrong'), 'error')
    console.error(error)
  }
}
