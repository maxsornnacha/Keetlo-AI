export type Project = {
  projectId: string;
  title: string;
  description: string;
  type: string;
  tags: string[];
  updatedAt: Date; 
  messages: Message[];
  isPublic: number;
  indexPage: string;
  mainHtmlContent: string;
}

export type Message = {
    projectMessageId: string;
    role: string;
    message: string;
    createdAt: Date;
}