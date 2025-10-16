export type Order = {
  currencySymbol?: string;                // safe id for links
  receiptId: string;
  name: string,
  email: string,
  startDate: string;
  endDate: string;
  createdAt: string;
  amount: number | string;  
  currency: string;
  status: string;
  planName: string;
  planDescription: string;
  receiptUrl?: string;
};