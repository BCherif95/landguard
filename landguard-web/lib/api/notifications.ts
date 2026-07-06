import { api } from "./client"

export interface NotificationPreferences {
  pushEnabled: boolean
  emailEnabled: boolean
  smsEnabled: boolean
  phoneNumber: string | null
}

export interface UpdateNotificationPreferencesPayload {
  pushEnabled: boolean
  emailEnabled: boolean
  smsEnabled: boolean
  phoneNumber: string | null
}

export const notificationsApi = {
  getPreferences: async (): Promise<NotificationPreferences> => {
    const { data } = await api.get<NotificationPreferences>("/notifications/preferences")
    return data
  },

  updatePreferences: async (
    payload: UpdateNotificationPreferencesPayload,
  ): Promise<NotificationPreferences> => {
    const { data } = await api.put<NotificationPreferences>("/notifications/preferences", payload)
    return data
  },
}
