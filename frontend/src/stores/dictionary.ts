import { defineStore } from 'pinia'
import { ref } from 'vue'
import { dictionaryApi, type Language, type Tag } from '@/api'

/** Languages and tags are loaded once and cached. */
export const useDictionaryStore = defineStore('dictionary', () => {
  const languages = ref<Language[]>([])
  const tags = ref<Tag[]>([])
  let loading: Promise<void> | null = null

  function load(force = false) {
    if (force) loading = null
    loading ??= Promise.all([dictionaryApi.languages(), dictionaryApi.tags()]).then(([l, t]) => {
      languages.value = l
      tags.value = t
    })
    return loading
  }

  function languageByCode(code: string) {
    return languages.value.find((l) => l.code === code)
  }

  return { languages, tags, load, languageByCode }
})
