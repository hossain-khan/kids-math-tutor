import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Builder from "./pages/Builder";
import Result from "./pages/Result";
import Help from "./pages/Help";
import SharedWorksheets from "./pages/SharedWorksheets";
import AdminManage from "./pages/AdminManage";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/builder/:type" element={<Builder />} />
        <Route path="/result" element={<Result />} />
        <Route path="/help" element={<Help />} />
        <Route path="/manage-worksheets" element={<AdminManage />} />
        <Route path="/worksheets" element={<SharedWorksheets />} />
        <Route path="/worksheets/:id" element={<SharedWorksheets />} />
      </Routes>
    </Router>
  );
}

export default App;
