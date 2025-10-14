import { toErrorMessage } from "@/utils/axios";
import { getToken, removeToken } from "@/utils/auth";
import { defineStore } from "pinia";
import axios from "axios";
import type { User } from "~/types/User";


type Result<T> = { ok: true; data: T } | { ok: false; error: string };

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
  }),
  actions: {
    async initializeUser() {
      const token = getToken();
      if (!token) {
        this.user = null;
        this.isAuthenticated = false;
        return { ok: false, error: "No token" } as Result<null>;
      }

      const res = await this.fetchUser();
      return res;
    },
    async fetchUser(): Promise<Result<User>> {
      try {
        const config = useRuntimeConfig();
        const response = await api.get(`${config.public.NUXT_PUBLIC_API_BASE}/user/me`);
        response.data.user.email = response.data.user.email ?? "Email not provided";
        this.user = response.data.user;
        this.isAuthenticated = true;
        return { ok: true, data: response.data };
      } catch (error: unknown) {
        let msg = "Server interval error, Please try again."
          if(axios.isAxiosError(error)){
             msg = toErrorMessage(error?.response?.data?.message);
            } 
          removeToken();
        return { ok: false, error: msg };
      }
    },
    logout() {
      removeToken();
      this.user = null;
      this.isAuthenticated = false;
    },
  },
});