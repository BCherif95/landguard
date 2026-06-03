import { motion } from "framer-motion"
import { Radio } from "lucide-react"

export function MonitoringRadar() {
  return (
    <div className="relative flex items-center justify-center w-full h-full min-h-[200px] bg-black/20 rounded-xl border border-primary/10 overflow-hidden">
      {/* Radar circles */}
      {[1, 2, 3].map((i) => (
        <motion.div
          key={i}
          className="absolute border border-primary/20 rounded-full"
          initial={{ width: 0, height: 0, opacity: 0.5 }}
          animate={{ 
            width: ["0%", "150%"], 
            height: ["0%", "150%"],
            opacity: [0.5, 0] 
          }}
          transition={{ 
            duration: 4, 
            delay: i * 1.3, 
            repeat: Infinity, 
            ease: "easeOut" 
          }}
        />
      ))}
      
      {/* Radar sweep */}
      <motion.div 
        className="absolute w-full h-full origin-center"
        animate={{ rotate: 360 }}
        transition={{ duration: 4, repeat: Infinity, ease: "linear" }}
      >
        <div className="absolute top-1/2 left-1/2 w-1/2 h-[2px] bg-gradient-to-r from-primary/50 to-transparent -translate-y-1/2" />
      </motion.div>

      <div className="relative z-10 flex flex-col items-center gap-2">
        <div className="p-3 bg-primary/20 rounded-full border border-primary/30 shadow-[0_0_20px_rgba(59,130,246,0.3)]">
          <Radio className="w-6 h-6 text-primary animate-pulse" />
        </div>
        <span className="text-[10px] font-mono text-primary uppercase tracking-[0.2em] animate-pulse">
          Balayage du Secteur Orbital
        </span>
      </div>
      
      {/* Decorative grid */}
      <div className="absolute inset-0 opacity-10 pointer-events-none" 
        style={{ 
          backgroundImage: 'linear-gradient(#3b82f6 1px, transparent 1px), linear-gradient(90deg, #3b82f6 1px, transparent 1px)',
          backgroundSize: '20px 20px'
        }} 
      />
    </div>
  )
}
