import { Link } from 'react-router-dom'

export default function Help() {
  return (
    <div className="min-h-screen">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <h1 className="text-2xl font-display font-bold text-gray-900">
            Help & Documentation
          </h1>
        </div>
      </header>
      
      <main className="container mx-auto px-4 py-8 max-w-3xl">
        <div className="card mb-6">
          <h2 className="text-xl font-display font-bold mb-4">Getting Started</h2>
          <p className="text-gray-600 mb-4">
            The Math Pup Worksheet Creator helps you create custom math practice problems
            for your child to solve in the Kids Math Pup Tutor Android app.
          </p>
          
          <h3 className="font-bold text-lg mb-2">Two Ways to Create:</h3>
          <ul className="space-y-2 text-gray-600">
            <li className="flex gap-2">
              <span>✨</span>
              <span><strong>Quick Generator:</strong> Set rules (operation, number range, problem count) and we generate problems for you</span>
            </li>
            <li className="flex gap-2">
              <span>✏️</span>
              <span><strong>Custom Problems:</strong> Enter each math problem exactly as you want it</span>
            </li>
          </ul>
        </div>

        <div className="card mb-6">
          <h2 className="text-xl font-display font-bold mb-4">How to Import to App</h2>
          <ol className="space-y-3 text-gray-700">
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-sm font-bold">
                1
              </span>
              <span>Create your worksheet and copy the generated code</span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-sm font-bold">
                2
              </span>
              <span>Open Kids Math Pup Tutor app on your Android device</span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-sm font-bold">
                3
              </span>
              <span>Go to Settings → Parent Challenges → Import</span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-sm font-bold">
                4
              </span>
              <span>Paste the code and tap "Save Challenge"</span>
            </li>
          </ol>
        </div>

        <div className="text-center">
          <Link to="/" className="btn-primary inline-block">
            Back to Home
          </Link>
        </div>
      </main>
    </div>
  )
}
