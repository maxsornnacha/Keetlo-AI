export interface PublicProject {
  projectId: string;
  title: string;
  description: string;
  indexPage: string;
  mainHtmlContent: string;
  user: {firstname: string, lastname: string, avatarUrl: string | null};
  link: string
  type: string
  tags: string[]
  favoriteCount: number
  remixCount: number
  publicAt: string 
  isFavorite?: boolean
}