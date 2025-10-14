export type SubscriptionPlan =  {
  subscriptionPlanId: string;
  name: string;
  description: string;
  price: number;
  requestsPerDay: number;
  advancedFeatures: number;
  prioritySupport: number;
  communitySupport: number;
  isActive: number;
  packageType: string;
  mostPopular: number;
  isVisible: number;
}