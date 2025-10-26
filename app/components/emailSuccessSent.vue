<template>
  <main
    class="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8"
  >
    <div class="w-full max-w-md space-y-8 text-center">
      <UIcon name="i-lucide-check-circle" class="size-24 text-green-500"/>
      <div>
        <h2
          class="text-3xl font-bold tracking-tight text-gray-900 dark:text-black"
        >
          Email Sent Successfully!
        </h2>
        <p class="mt-2 text-base text-gray-500">
          We've sent a confirmation email to your inbox. Please check it for the
          next steps.
        </p>
      </div>
      <div class="space-y-4">
        <a 
          href="/"
          class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-indigo-500 hover:bg-indigo-600"
        >
          Back to Home
      </a>
        <a
          href="/auth/forgot-password"
          class="w-full flex justify-center py-3 px-4 border border-gray-200 hover:bg-gray-200 rounded-lg text-sm font-medium text-gray-700 bg-white"
        >
          Didn't receive it? Resend Email
        </a>
      </div>
    </div>
  </main>
</template>
