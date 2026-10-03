<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Trash2, X } from 'lucide-vue-next'
import { adminApi, type Sheet, type User } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictionaryStore } from '@/stores/dictionary'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import { formatDate } from '@/utils/format'

const { t, locale } = useI18n()
const auth = useAuthStore()
const dictionary = useDictionaryStore()
const ui = useUiStore()
const showError = useErrorToast()

const tab = ref<'users' | 'languages' | 'tags'>('users')
const users = ref<Sheet<User> | null>(null)
const page = ref(0)
const newTag = ref('')
const language = reactive({ code: '', name: '', flag: '' })

async function loadUsers() {
  try {
    users.value = await adminApi.users(page.value, 20)
  } catch (e) {
    showError(e)
  }
}

async function setRole(user: User, role: 'user' | 'admin') {
  try {
    Object.assign(user, await adminApi.setRole(user.id, role))
    ui.toast(t('common.saved'), 'success')
  } catch (e) {
    showError(e)
    await loadUsers()
  }
}

async function removeUser(user: User) {
  const ok = await ui.confirm({ title: t('common.delete'), text: t('admin.deleteUserConfirm', { name: user.username }), danger: true })
  if (!ok) return
  try {
    await adminApi.deleteUser(user.id)
    await loadUsers()
  } catch (e) {
    showError(e)
  }
}

async function addTag() {
  if (newTag.value.trim().length < 2) return
  try {
    await adminApi.createTag(newTag.value.trim())
    newTag.value = ''
    await dictionary.load(true)
  } catch (e) {
    showError(e)
  }
}

async function removeTag(id: number) {
  try {
    await adminApi.deleteTag(id)
    await dictionary.load(true)
  } catch (e) {
    showError(e)
  }
}

async function addLanguage() {
  try {
    await adminApi.createLanguage({ code: language.code.trim(), name: language.name.trim(), flag: language.flag.trim() || undefined })
    Object.assign(language, { code: '', name: '', flag: '' })
    await dictionary.load(true)
  } catch (e) {
    showError(e)
  }
}

async function removeLanguage(id: number) {
  try {
    await adminApi.deleteLanguage(id)
    await dictionary.load(true)
  } catch (e) {
    showError(e)
  }
}

function go(delta: number) {
  page.value += delta
  loadUsers()
}

onMounted(() => {
  loadUsers()
  dictionary.load(true)
})
</script>

<template>
  <div class="page">
    <h1>🛡️ {{ $t('admin.title') }}</h1>

    <div class="row-wrap">
      <button v-for="tb in (['users', 'languages', 'tags'] as const)" :key="tb" type="button" class="chip"
              :class="{ 'is-active': tab === tb }" @click="tab = tb">{{ $t(`admin.${tb}`) }}</button>
    </div>

    <section v-if="tab === 'users'" class="glass card-pad stack">
      <div class="scroll">
        <table class="table">
          <thead>
            <tr>
              <th>{{ $t('auth.username') }}</th>
              <th>{{ $t('auth.email') }}</th>
              <th>{{ $t('admin.role') }}</th>
              <th>{{ $t('admin.registered') }}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users?.content ?? []" :key="u.id">
              <td class="bold">{{ u.username }}</td>
              <td class="muted">{{ u.email }}</td>
              <td>
                <select class="select role" :value="u.role" :disabled="u.id === auth.user?.id"
                        @change="setRole(u, ($event.target as HTMLSelectElement).value as 'user' | 'admin')">
                  <option value="user">user</option>
                  <option value="admin">admin</option>
                </select>
              </td>
              <td class="small muted nowrap">{{ formatDate(u.createdAt, locale) }}</td>
              <td>
                <button v-if="u.id !== auth.user?.id" class="btn btn-danger-ghost btn-icon btn-sm" type="button" @click="removeUser(u)">
                  <Trash2 :size="16" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="users" class="row">
        <button class="btn btn-sm" type="button" :disabled="users.page.first" @click="go(-1)">←</button>
        <span class="small muted">{{ users.page.number + 1 }} / {{ Math.max(1, users.page.totalPages) }} · {{ users.page.totalElements }}</span>
        <button class="btn btn-sm" type="button" :disabled="users.page.last" @click="go(1)">→</button>
      </div>
    </section>

    <section v-if="tab === 'languages'" class="glass card-pad stack">
      <form class="row-wrap" @submit.prevent="addLanguage">
        <input v-model="language.code" class="input short" :placeholder="$t('admin.code')" maxlength="10" />
        <input v-model="language.name" class="input mid" :placeholder="$t('admin.name')" maxlength="255" />
        <input v-model="language.flag" class="input short" :placeholder="$t('admin.flag')" maxlength="16" />
        <button class="btn btn-primary" type="submit" :disabled="language.code.length < 2 || language.name.length < 2"><Plus :size="18" /></button>
      </form>
      <div class="row-wrap">
        <span v-for="l in dictionary.languages" :key="l.id" class="chip">
          {{ l.flag }} {{ l.name }} <span class="subtle">({{ l.code }})</span>
          <button class="x" type="button" @click="removeLanguage(l.id)"><X :size="14" /></button>
        </span>
      </div>
    </section>

    <section v-if="tab === 'tags'" class="glass card-pad stack">
      <form class="row" @submit.prevent="addTag">
        <input v-model="newTag" class="input mid" :placeholder="$t('admin.newTag')" maxlength="50" />
        <button class="btn btn-primary" type="submit"><Plus :size="18" /></button>
      </form>
      <div class="row-wrap">
        <span v-for="tag in dictionary.tags" :key="tag.id" class="chip">
          #{{ tag.name }}
          <button class="x" type="button" @click="removeTag(tag.id)"><X :size="14" /></button>
        </span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.scroll {
  overflow-x: auto;
}

.role {
  height: 38px;
  width: 120px;
}

.short {
  width: 110px;
}

.mid {
  width: 240px;
}

.x {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: var(--surface-sunken);
  color: var(--text-muted);
}

.x:hover {
  background: var(--danger-soft);
  color: var(--danger);
}
</style>
