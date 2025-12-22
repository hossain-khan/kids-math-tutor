import { useParams } from 'react-router-dom';
import GeneratedBuilder from './GeneratedBuilder';
import ExplicitBuilder from './ExplicitBuilder';

export default function Builder() {
  const { type } = useParams<{ type: string }>();

  if (type === 'generated') {
    return <GeneratedBuilder />;
  }

  if (type === 'explicit') {
    return <ExplicitBuilder />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-2xl font-display font-bold text-gray-900 mb-4">
          Unknown Builder Type
        </h1>
        <p className="text-gray-600">
          Please select a builder type from the home page.
        </p>
      </div>
    </div>
  );
}
