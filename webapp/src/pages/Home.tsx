import { Link } from "react-router-dom";

export default function Home() {
  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center gap-3">
            <img
              src="/images/logo.webp"
              alt="Math Pup Logo"
              className="w-12 h-12 object-contain"
            />
            <h1 className="text-2xl font-display font-bold text-gray-900">
              Math Pup Worksheet Creator
            </h1>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        {/* Hero Section */}
        <section className="text-center mb-12">
          <img
            src="/images/hero.webp"
            alt="Math Pup Teaching"
            className="w-48 h-48 mx-auto mb-6 object-contain animate-bounce-slow"
          />
          <h2 className="text-4xl md:text-5xl font-display font-bold text-gray-900 mb-4">
            Math Pup Worksheet Creator
          </h2>
          <p className="text-xl text-gray-600 mb-8">
            Create custom math practice for your child in seconds!
          </p>
        </section>

        {/* Builder Type Selection */}
        <section className="grid md:grid-cols-2 gap-6 mb-12">
          <Link to="/builder/generated">
            <div className="card hover:shadow-lg transition-shadow cursor-pointer h-full border-2 hover:border-primary-300">
              <div className="text-5xl mb-4">✨</div>
              <h3 className="text-2xl font-display font-bold text-gray-900 mb-3">
                Quick Generator
              </h3>
              <p className="text-gray-600 mb-4">
                Set rules and let us create problems automatically
              </p>
              <div className="flex items-center text-primary-600 font-medium">
                <span>Get Started</span>
                <span className="ml-2">→</span>
              </div>
            </div>
          </Link>

          <Link to="/builder/explicit">
            <div className="card hover:shadow-lg transition-shadow cursor-pointer h-full border-2 hover:border-secondary-300">
              <div className="text-5xl mb-4">✏️</div>
              <h3 className="text-2xl font-display font-bold text-gray-900 mb-3">
                Custom Problems
              </h3>
              <p className="text-gray-600 mb-4">
                Enter each problem exactly how you want it
              </p>
              <div className="flex items-center text-secondary-600 font-medium">
                <span>Get Started</span>
                <span className="ml-2">→</span>
              </div>
            </div>
          </Link>
        </section>

        {/* Features Section */}
        <section className="grid md:grid-cols-3 gap-6 mb-12">
          <div className="card text-center">
            <div className="text-4xl mb-3">✅</div>
            <h3 className="font-display font-bold text-lg mb-2">
              Instant Validation
            </h3>
            <p className="text-gray-600 text-sm">
              No errors when importing to the app
            </p>
          </div>

          <div className="card text-center">
            <div className="text-4xl mb-3">📱</div>
            <h3 className="font-display font-bold text-lg mb-2">
              Mobile Friendly
            </h3>
            <p className="text-gray-600 text-sm">
              Works perfectly on phones and tablets
            </p>
          </div>

          <div className="card text-center">
            <div className="text-4xl mb-3">🎨</div>
            <h3 className="font-display font-bold text-lg mb-2">Fun Themes</h3>
            <p className="text-gray-600 text-sm">
              Engaging design kids will love
            </p>
          </div>
        </section>

        {/* How It Works */}
        <section className="card bg-gradient-to-br from-primary-50 to-secondary-50 border-2 border-primary-200">
          <h2 className="text-2xl font-display font-bold text-gray-900 mb-6 text-center">
            How It Works
          </h2>
          <div className="space-y-4">
            <div className="flex gap-4 items-start">
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary-500 text-white flex items-center justify-center font-bold">
                1
              </div>
              <div>
                <h3 className="font-bold text-gray-900 mb-1">
                  Choose Your Style
                </h3>
                <p className="text-gray-600 text-sm">
                  Pick Quick Generator for automatic problems or Custom Problems
                  for full control
                </p>
              </div>
            </div>

            <div className="flex gap-4 items-start">
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary-500 text-white flex items-center justify-center font-bold">
                2
              </div>
              <div>
                <h3 className="font-bold text-gray-900 mb-1">
                  Fill in the Details
                </h3>
                <p className="text-gray-600 text-sm">
                  Enter your challenge title, choose operations, and set up
                  problems
                </p>
              </div>
            </div>

            <div className="flex gap-4 items-start">
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary-500 text-white flex items-center justify-center font-bold">
                3
              </div>
              <div>
                <h3 className="font-bold text-gray-900 mb-1">
                  Generate & Share
                </h3>
                <p className="text-gray-600 text-sm">
                  Copy the code and paste it into the Kids Math Pup Tutor app
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-16">
        <div className="container mx-auto px-4 py-6 text-center text-gray-600 text-sm">
          <p>Made with ❤️ for Kids Math Pup Tutor</p>
          <Link
            to="/help"
            className="text-primary-600 hover:underline mt-2 inline-block"
          >
            Need Help?
          </Link>
        </div>
      </footer>
    </div>
  );
}
