export type User = {
  
  email: string;
  firstname: string;
  lastname: string;
  avatarUrl: string | null;
  isLogin: string;
  limitRequests: number | null;
  leftRequests: number | null;
  subscriptionPlanName: string;
  isGoogleConnected: number;
  isGithubConnected: number;
};