<template>
  <main
    class="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8"
  >
    <div class="w-full max-w-md space-y-8">
      <div>
        <h2
          class="mt-6 text-center text-3xl font-bold tracking-tight text-gray-900 dark:text-white"
        >
          Change Your Password
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
          Update your password for better security.
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="onSubmit">
        <div class="rounded-lg shadow-sm -space-y-px">
          <div>
            <label class="sr-only" for="new-password">New Password</label>
            <input
              id="new-password"
              v-model="form.newPassword"
              autocomplete="new-password"
              class="appearance-none rounded-t-lg relative block w-full px-3 py-4 border border-gray-300 dark:border-gray-700 bg-background-light dark:bg-background-dark placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm"
              name="new-password"
              placeholder="New Password"
              required
              type="password"
            >
          </div>
          <div>
            <label class="sr-only" for="confirm-new-password"
              >Confirm New Password</label
            >
            <input
              id="confirm-new-password"
              v-model="form.confirmPassword"
              autocomplete="new-password"
              class="appearance-none rounded-b-lg relative block w-full px-3 py-4 border border-gray-300 dark:border-gray-700 bg-background-light dark:bg-background-dark placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm"
              name="confirm-new-password"
              placeholder="Confirm New Password"
              required
              type="password"
            >
          </div>
        </div>
        <div class="text-xs text-gray-500 dark:text-gray-400">
          <p>
            Password must be at least 8 characters long and include a mix of
            uppercase and lowercase letters, numbers, and symbols.
          </p>
        </div>
        <div v-if="errorMessage" class="my-2 text-red-500 self-start">
          <p>{{ errorMessage }}</p>
        </div>
        <div>
          <button
            :disabled="loadingSubmit || !onValidate()"
            class="flex min-w-[84px] w-full items-center justify-center overflow-hidden rounded-md h-12 px-4 transition-colors text-white text-base font-bold leading-normal tracking-[0.015em]"
            :class="
              loadingSubmit || !onValidate()
                ? 'bg-indigo-400 cursor-default'
                : 'bg-indigo-600 hover:bg-indigo-700 cursor-pointer'
            "
          >
            <span v-if="loadingSubmit" class="flex items-center gap-2">
              <UIcon
                name="i-lucide-loader-circle"
                class="size-7 text-white animate-spin"
              />
              <span>Loading</span>
            </span>
            <span v-else>Change Password</span>
          </button>
        </div>
      </form>
    </div>
  </main>
</template>
<script setup lang="ts">
import axios from "axios";

const route = useRoute();
const router = useRouter();
const config = useRuntimeConfig();
const token = computed(() => route.query.token);
const form = reactive({
  newPassword: "",
  confirmPassword: "",
});
const errorMessage = ref<string | null>(null);
const loadingSubmit = ref(false);
const { $modal } = useNuxtApp();

const onValidate = () => {
  return form.newPassword && form.confirmPassword && token;
};

function validatePassword(password: string) {
  const minLength = 8;
  const hasLetters = /[a-zA-Z]/.test(password);
  const hasNumbers = /\d/.test(password);
  const hasSymbols = /[!@#$%^&*(),.?":{}|<>]/.test(password);

  if (password.length < minLength) return false;
  if (!hasLetters || !hasNumbers || !hasSymbols) return false;

  return true;
}

const onSubmit = async () => {
  const ok = await $modal.confirm({
    title: "Confirm",
    html: "<p>Are you sure you would like to change password?</p>",
    confirmText: "Confirm",
    cancelText: "Cancel",
    variant: "default",
  });
  if (!ok) {
    return;
  }

  if (!validatePassword(form.newPassword)) {
    errorMessage.value = "Please enter your password.";
    return;
  }
  if (form.confirmPassword === "") {
    errorMessage.value = "Please enter your password confirmation.";
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = "Passwords do not match.";
    return;
  }

  try {
    loadingSubmit.value = true;
    const payload = {
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
      token: token.value,
    };
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/auth/reset-password`,
      payload
    );
    onLogout();
      const successOk =   await $modal.alert({ 
      title: 'Success', 
      html: `<p>Password got changed successfully!</p>`, 
      variant: 'success' 
    });
    if(successOk){
      router.push("/auth/login");
    }
    setTimeout(()=>{
      router.push("/auth/login");
    }, 1500)
    errorMessage.value = null;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
    }
    loadingSubmit.value = false;
  }
};

const onLogout = async () => {
  await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/logout`);
};
</script>
