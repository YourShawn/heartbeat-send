import { Navigate, Route, Routes } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "./auth";
import { Shell } from "./components/Shell";
import { useI18n } from "./i18n-context";
import { CustomPage } from "./pages/CustomPage";
import { LibraryPage } from "./pages/LibraryPage";
import { LoginPage } from "./pages/LoginPage";
import { PlayPage } from "./pages/PlayPage";
import { SharedPage } from "./pages/SharedPage";
import { SynthPage } from "./pages/SynthPage";
import { WearablePage } from "./pages/WearablePage";

function Guard({ children }: { children: ReactNode }) {
  const { ready, profile } = useAuth();
  const { copy } = useI18n();
  if (!ready) {
    return <p style={{ padding: 24 }}>{copy.loading}</p>;
  }
  if (!profile) {
    return <Navigate to="/login" replace />;
  }
  return <Shell>{children}</Shell>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/s/:token" element={<SharedPage />} />
      <Route path="/" element={<Guard><LibraryPage /></Guard>} />
      <Route path="/custom" element={<Guard><CustomPage /></Guard>} />
      <Route path="/synth" element={<Guard><SynthPage /></Guard>} />
      <Route path="/wearable" element={<Guard><WearablePage /></Guard>} />
      <Route path="/play/:id" element={<Guard><PlayPage /></Guard>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
