"use client"

import React, { Component, ErrorInfo, ReactNode } from "react"
import { AlertCircle, RefreshCcw, Home } from "lucide-react"
import { Button } from "@/components/ui/button"

interface Props {
  children?: ReactNode
  fallback?: ReactNode
  name?: string
}

interface State {
  hasError: boolean
  error: Error | null
}

export class AppErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null
  }

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error(`Uncaught error in ${this.props.name || 'Component'}:`, error, errorInfo)
  }

  private handleReset = () => {
    this.setState({ hasError: false, error: null })
  }

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback
      }

      return (
        <div className="flex min-h-[200px] w-full flex-col items-center justify-center rounded-xl border border-dashed border-red-500/30 bg-red-500/5 p-8 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-500/10 text-red-500">
            <AlertCircle className="h-6 w-6" />
          </div>
          <h2 className="mt-4 font-display text-base font-medium text-foreground">
            Une erreur est survenue dans {this.props.name || 'cette section'}
          </h2>
          <p className="mt-2 max-w-xs text-xs text-muted-foreground">
            L&apos;interface a rencontré un problème inattendu lors du rendu des données.
          </p>
          <div className="mt-6 flex gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={this.handleReset}
              className="h-8 gap-2 border-red-500/20 bg-background/50 hover:bg-red-500/10"
            >
              <RefreshCcw className="h-3.5 w-3.5" />
              Réessayer
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.location.href = '/dashboard'}
              className="h-8 gap-2"
            >
              <Home className="h-3.5 w-3.5" />
              Dashboard
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
