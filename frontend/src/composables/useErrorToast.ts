import { useI18n } from 'vue-i18n'
import { ApiError } from '@/api'
import { useUiStore } from '@/stores/ui'

/** Shows a human-friendly toast for an API error. */
export function useErrorToast() {
  const ui = useUiStore()
  const { t } = useI18n()

  return (error: unknown) => {
    if (error instanceof ApiError) {
      if (error.status === 0) {
        ui.toast(t('common.networkError'), 'error')
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
