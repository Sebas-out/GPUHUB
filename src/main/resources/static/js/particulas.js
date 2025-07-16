// Configuración de las partículas
const config = {
    // Cantidad de partículas
    particulas: 100,
    // Color de las partículas (blanco con transparencia)
    color: 'rgba(255, 255, 255, 0.7)',
    // Radio mínimo y máximo de las partículas
    radioMin: 1,
    radioMax: 3,
    // Velocidad de movimiento
    velocidad: 0.5,
    // Distancia de conexión entre partículas
    distanciaConexion: 150,
    // Color de las líneas de conexión
    colorConexion: 'rgba(255, 255, 255, 0.2)'
};

// Inicialización del canvas
document.addEventListener('DOMContentLoaded', () => {
    // Esperar a que todo el DOM esté listo
    setTimeout(() => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        const contenedor = document.getElementById('particulas-container') || document.body;
        
        // Configuración del canvas
        canvas.style.position = 'fixed';
        canvas.style.top = '0';
        canvas.style.left = '0';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.style.zIndex = '-1';
        canvas.style.pointerEvents = 'none';
        
        // Asegurarse de que el canvas esté detrás de todo
        contenedor.insertBefore(canvas, contenedor.firstChild);
        
        // Ajustar tamaño del canvas
        function ajustarCanvas() {
            const width = window.innerWidth;
            const height = window.innerHeight;
            
            // Asegurar que el canvas tenga el tamaño correcto
            if (canvas.width !== width || canvas.height !== height) {
                canvas.width = width;
                canvas.height = height;
            }
        }
        
        // Llamar a ajustarCanvas inmediatamente
        ajustarCanvas();
        
        // Escuchar cambios de tamaño
        window.addEventListener('resize', ajustarCanvas);
    
    // Clase para las partículas
    class Particula {
        constructor() {
            // Posición inicial aleatoria en toda la pantalla
            this.x = Math.random() * window.innerWidth;
            this.y = Math.random() * window.innerHeight;
            this.radio = Math.random() * (config.radioMax - config.radioMin) + config.radioMin;
            this.vx = (Math.random() - 0.5) * config.velocidad;
            this.vy = (Math.random() - 0.5) * config.velocidad;
        }
        
        actualizar() {
            // Mover partícula
            this.x += this.vx;
            this.y += this.vy;
            
            // Rebotar en los bordes
            if (this.x < 0 || this.x > canvas.width) this.vx = -this.vx;
            if (this.y < 0 || this.y > canvas.height) this.vy = -this.vy;
        }
        
        dibujar() {
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radio, 0, Math.PI * 2);
            ctx.fillStyle = config.color;
            ctx.fill();
        }
    }
    
    // Crear partículas
    const particulas = [];
    // Aumentar el número de partículas para mejor cobertura
    for (let i = 0; i < config.particulas * 2; i++) {
        particulas.push(new Particula());
    }
    
    // Función para dibujar líneas entre partículas cercanas
    function dibujarConexiones() {
        for (let i = 0; i < particulas.length; i++) {
            for (let j = i + 1; j < particulas.length; j++) {
                const dx = particulas[i].x - particulas[j].x;
                const dy = particulas[i].y - particulas[j].y;
                const distancia = Math.sqrt(dx * dx + dy * dy);
                
                if (distancia < config.distanciaConexion) {
                    ctx.beginPath();
                    ctx.strokeStyle = config.colorConexion;
                    ctx.lineWidth = 0.5;
                    ctx.moveTo(particulas[i].x, particulas[i].y);
                    ctx.lineTo(particulas[j].x, particulas[j].y);
                    ctx.stroke();
                }
            }
        }
    }
    
    // Bucle de animación
    function animar() {
        // Limpiar canvas con un fondo semi-transparente para el efecto de estela
        ctx.fillStyle = 'rgba(26, 0, 26, 0.05)';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Actualizar y dibujar partículas
        particulas.forEach(particula => {
            particula.actualizar();
            particula.dibujar();
        });
        
        // Dibujar conexiones
        dibujarConexiones();
        
        // Siguiente frame
        requestAnimationFrame(animar);
    }
    
        // Iniciar animación
        animar();
    }, 100); // Pequeño retraso para asegurar que el DOM esté listo
});
