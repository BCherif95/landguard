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

  const connect = useCallback(() => {
    if (!accessToken) return
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
    }

    const url = new URL(monitoringApi.getStreamUrl())
    url.searchParams.append("token", accessToken)

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

    es.onerror = (err) => {
      const state = es.readyState
      console.error(`[SSE] Erreur de connexion (État: ${state})`, err)
      
      setIsConnected(false)
      es.close()
      
      // If unauthorized (often indicated by immediate closure in some browsers)
      // or if we reached max attempts, maybe show a warning
      
      const delay = RECONNECT_INTERVALS[Math.min(reconnectAttemptRef.current, RECONNECT_INTERVALS.length - 1)]
      console.log(`[SSE] Tentative de reconnexion dans ${delay}ms (Essai #${reconnectAttemptRef.current + 1})`)
      
      reconnectTimeoutRef.current = setTimeout(() => {
        reconnectAttemptRef.current += 1
        connect()
      }, delay)
    }
  }, [accessToken, addEvent])

  useEffect(() => {
    connect()

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close()
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current)
      }
    }
  }, [connect])

  return {
    isConnected
  }
}
