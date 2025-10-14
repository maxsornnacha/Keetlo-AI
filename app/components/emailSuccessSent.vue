<template>
  <main
    class="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8"
  >
    <div class="w-full max-w-md space-y-8 text-center">
      <UIcon name="i-lucide-check-circle" class="size-24 text-green-500"/>
      <div>
        <h2
          class="text-3xl font-bold tracking-tight text-slate-900 dark:text-white"
        >
          Email Sent Successfully!
        </h2>
        <p class="mt-2 text-base text-slate-500 dark:text-slate-400">
          We've sent a confirmation email to your inbox. Please check it for the
          next steps.
        </p>
      </div>
      <div class="space-y-4">
        <a 
          href="/"
          class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
        >
          Back to Home
      </a>
        <a
          href="/auth/forgot-password"
          class="w-full flex justify-center py-3 px-4 border rounded-lg text-sm font-medium text-slate-700 dark:text-slate-300 bg-transparent hover:text-blue-500"
        >
          Didn't receive it? Resend Email
        </a>
      </div>
    </div>
  </main>
</template>
