import { SatelliteSnapshot } from "@/lib/api/types"
import { Card } from "@/components/ui/card"
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area"
import { format } from "date-fns"
import { Camera, Layers } from "lucide-react"

interface SatelliteTimelineProps {
  snapshots: SatelliteSnapshot[]
  onSelect?: (snapshot: SatelliteSnapshot) => void
  selectedId?: string
}

export function SatelliteTimeline({ snapshots, onSelect, selectedId }: SatelliteTimelineProps) {
  return (
    <Card className="bg-black/40 backdrop-blur-xl border-primary/20 overflow-hidden">
      <div className="p-3 border-b flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Camera className="w-4 h-4 text-primary" />
          <h4 className="text-[10px] font-black uppercase tracking-widest">Timeline Orbitale</h4>
        </div>
        <div className="flex items-center gap-1">
          <Layers className="w-3 h-3 text-muted-foreground" />
          <span className="text-[10px] text-muted-foreground font-mono">{snapshots.length} CAPTURES</span>
        </div>
      </div>
      
      <ScrollArea className="w-full">
        <div className="flex p-4 gap-4">
          {snapshots.map((snap) => (
            <div 
              key={snap.id}
              className={`flex-shrink-0 w-32 cursor-pointer group transition-all ${selectedId === snap.id ? 'scale-105' : 'hover:scale-105'}`}
              onClick={() => onSelect?.(snap)}
            >
              <div className={`aspect-square rounded-lg overflow-hidden border-2 mb-2 ${selectedId === snap.id ? 'border-primary shadow-[0_0_10px_rgba(59,130,246,0.5)]' : 'border-white/10'}`}>
                <img src={snap.imageUrl} alt="" className="w-full h-full object-cover grayscale" />
              </div>
              <p className="text-[10px] font-mono text-center truncate">
                {format(new Date(snap.capturedAt), "dd MMM HH:mm")}
              </p>
              <div className="flex justify-center gap-1 mt-1">
                <div className="h-1 w-full bg-white/10 rounded-full overflow-hidden">
                  <div className="h-full bg-primary" style={{ width: `${snap.anomalyScore}%` }} />
                </div>
              </div>
            </div>
          ))}
        </div>
        <ScrollBar orientation="horizontal" />
      </ScrollArea>
    </Card>
  )
}
