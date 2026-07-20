import { useEffect, useRef, useState, useCallback } from "react"
import { useMonitoringStore } from "../store/monitoring-store"
import { monitoringApi } from "../api/monitoring"
import { useAuthStore } from "../store/auth-store"
import { toast } from "sonner"
import { MonitoringEvent } from "../api/types"

const RECONNECT_INTERVALS = [1000, 2000, 5000, 10000, 30000]

export const useMonitoringStream = () => {
  const { addEvent } = useMonitoringStore()
  const { accessToken } = useAuthStore()
  const [isConnected, setIsConnected] = useState(false)
  const reconnectAttemptRef = useRef(0)
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null)
  const eventSourceRef = useRef<EventSource | null>(null)
  // Set on unmount so a late error from a torn-down stream never reconnects.
  const stoppedRef = useRef(false)

  const connect = useCallback(() => {
    // Always read the freshest token: on reconnect the captured one may have
    // been rotated by the axios refresh interceptor in the meantime.
    const token = useAuthStore.getState().accessToken
    if (!token) return
    stoppedRef.current = false
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
    }

    const url = new URL(monitoringApi.getStreamUrl())
    url.searchParams.append("token", token)

    console.log("[SSE] Connexion au flux de surveillance...")
    const es = new EventSource(url.toString(), { withCredentials: true })
    eventSourceRef.current = es

    es.onopen = () => {
      console.log("[SSE] Flux connecté avec succès")
      setIsConnected(true)
      reconnectAttemptRef.current = 0
    }

    es.addEventListener("monitoring-event", (event: MessageEvent) => {
      try {
        const data: MonitoringEvent = JSON.parse(event.data)
        addEvent(data)
        
        const typeLabel = data.type?.replace(/_/g, ' ') || "ALERTE"
        
        if (data.severity === "CRITICAL" || data.severity === "HIGH") {
          toast.error(`ALERTE : ${typeLabel}`, {
            description: data.description,
          })
        } else {
          toast.info(`Événement : ${typeLabel}`, {
            description: data.description,
          })
        }
      } catch (err) {
        console.error("[SSE] Erreur de parsing des données", err)
      }
    })

    es.addEventListener("heartbeat", () => {
      // Keep alive silent
    })

    es.addEventListener("init", () => {
      console.log("[SSE] Signal d'initialisation reçu")
    })

    es.onerror = () => {
      // Ignore errors from a stream we already tore down or replaced. In dev
      // (StrictMode double-mount) and on token rotation, an old EventSource can
      // fire onerror after being superseded — reconnecting on it would churn.
      if (stoppedRef.current || eventSourceRef.current !== es) {
        es.close()
        return
      }

      setIsConnected(false)
      es.close()

      // An interrupted stream (server restart, network blip, keep-alive
      // timeout) is a normal part of the SSE lifecycle — reconnect with
      // backoff and log a single informational line.
      const delay = RECONNECT_INTERVALS[Math.min(reconnectAttemptRef.current, RECONNECT_INTERVALS.length - 1)]
      console.info(`[SSE] Flux de surveillance interrompu — reconnexion dans ${delay / 1000}s (essai #${reconnectAttemptRef.current + 1})`)

      reconnectTimeoutRef.current = setTimeout(() => {
        reconnectAttemptRef.current += 1
        connect()
      }, delay)
    }
  }, [addEvent])

  // `accessToken` is the trigger: (re)connect on login and token rotation.
  useEffect(() => {
    connect()

    return () => {
      stoppedRef.current = true
      if (eventSourceRef.current) {
        eventSourceRef.current.close()
        eventSourceRef.current = null
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current)
        reconnectTimeoutRef.current = null
      }
    }
  }, [connect, accessToken])

  return {
    isConnected
  }
}
