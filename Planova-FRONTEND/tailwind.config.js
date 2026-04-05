/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Kalam', 'cursive', 'system-ui', 'sans-serif'],
      },
      colors: {
        glass: {
          50: 'rgba(255, 255, 255, 0.05)',
          100: 'rgba(255, 255, 255, 0.1)',
          150: 'rgba(255, 255, 255, 0.15)',
          200: 'rgba(255, 255, 255, 0.2)',
          250: 'rgba(255, 255, 255, 0.25)',
          300: 'rgba(255, 255, 255, 0.3)',
          350: 'rgba(255, 255, 255, 0.35)',
          400: 'rgba(255, 255, 255, 0.4)',
          450: 'rgba(255, 255, 255, 0.45)',
          500: 'rgba(255, 255, 255, 0.5)',
        },
        surface: {
          dark: 'rgba(15, 23, 42, 0.6)',
          darker: 'rgba(15, 23, 42, 0.8)',
        }
      },
      backdropBlur: {
        'glass': '20px',
        'glass-heavy': '40px',
        'glass-light': '12px',
      },
      boxShadow: {
        'glass': '0 8px 32px 0 rgba(31, 38, 135, 0.37)',
        'glass-glow': '0 0 20px rgba(255, 255, 255, 0.1), 0 8px 32px 0 rgba(31, 38, 135, 0.37)',
        'glass-float': '0 20px 40px rgba(0, 0, 0, 0.2), 0 0 1px rgba(255, 255, 255, 0.1)',
      },
      borderRadius: {
        'glass': '16px',
        'glass-lg': '24px',
      },
      animation: {
        'float': 'float 6s ease-in-out infinite',
        'pulse-glow': 'pulse-glow 4s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        'pulse-glow': {
          '0%, 100%': { boxShadow: '0 0 20px rgba(255, 255, 255, 0.1)' },
          '50%': { boxShadow: '0 0 40px rgba(255, 255, 255, 0.2)' },
        },
      },
    },
  },
  plugins: [],
}