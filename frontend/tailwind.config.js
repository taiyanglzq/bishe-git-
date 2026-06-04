/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          900: '#064e3b'
        }
      },
      borderRadius: {
        'card': '14px',
        'xl': '20px'
      }
    },
  },
  plugins: [],
  corePlugins: {
    preflight: false  // 禁用 Tailwind 的 reset，避免与 Element Plus 冲突
  }
}
