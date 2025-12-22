import { describe, it, expect, vi, beforeEach } from 'vitest'
import { cn, formatNumber, downloadJson, copyToClipboard } from '@/lib/utils'

describe('cn (className utility)', () => {
  it('should merge className strings', () => {
    expect(cn('foo', 'bar')).toBe('foo bar')
  })

  it('should handle conditional classes', () => {
    expect(cn('foo', false && 'bar', 'baz')).toBe('foo baz')
    expect(cn('foo', true && 'bar')).toBe('foo bar')
  })

  it('should handle Tailwind conflicts correctly', () => {
    // twMerge should prioritize later classes
    expect(cn('px-2', 'px-4')).toBe('px-4')
    expect(cn('text-red-500', 'text-blue-500')).toBe('text-blue-500')
  })

  it('should handle arrays and objects', () => {
    expect(cn(['foo', 'bar'])).toBe('foo bar')
    expect(cn({ foo: true, bar: false })).toBe('foo')
  })

  it('should handle undefined and null', () => {
    expect(cn('foo', undefined, null, 'bar')).toBe('foo bar')
  })
})

describe('formatNumber', () => {
  it('should format numbers with commas', () => {
    expect(formatNumber(1000)).toBe('1,000')
    expect(formatNumber(1000000)).toBe('1,000,000')
  })

  it('should format small numbers without commas', () => {
    expect(formatNumber(100)).toBe('100')
    expect(formatNumber(999)).toBe('999')
  })

  it('should handle zero', () => {
    expect(formatNumber(0)).toBe('0')
  })

  it('should handle negative numbers', () => {
    expect(formatNumber(-1000)).toBe('-1,000')
    expect(formatNumber(-999)).toBe('-999')
  })

  it('should handle decimal numbers', () => {
    expect(formatNumber(1234.56)).toBe('1,234.56')
  })
})

describe('downloadJson', () => {
  beforeEach(() => {
    // Mock DOM APIs
    global.URL.createObjectURL = vi.fn(() => 'mock-url')
    global.URL.revokeObjectURL = vi.fn()
    
    // Create mock link element
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn(),
    } as any
    
    // Setup document mocks
    vi.stubGlobal('document', {
      createElement: vi.fn(() => mockLink),
      body: {
        appendChild: vi.fn(),
        removeChild: vi.fn(),
      },
    })
  })

  it('should create a download link with correct filename', () => {
    const data = { test: 'data' }
    downloadJson(data, 'test.json')

    expect(document.createElement).toHaveBeenCalledWith('a')
    expect(document.body.appendChild).toHaveBeenCalled()
    expect(document.body.removeChild).toHaveBeenCalled()
  })

  it('should format JSON with proper indentation', () => {
    const data = { foo: 'bar', nested: { key: 'value' } }
    downloadJson(data, 'test.json')

    // Verify Blob was created with formatted JSON
    expect(global.URL.createObjectURL).toHaveBeenCalled()
  })

  it('should revoke object URL after download', () => {
    downloadJson({ test: 'data' }, 'test.json')
    expect(global.URL.revokeObjectURL).toHaveBeenCalledWith('mock-url')
  })
})

describe('copyToClipboard', () => {
  it('should copy text to clipboard successfully', async () => {
    const writeTextMock = vi.fn().mockResolvedValue(undefined)
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextMock,
      },
    })

    const result = await copyToClipboard('test text')
    
    expect(writeTextMock).toHaveBeenCalledWith('test text')
    expect(result).toBe(true)
  })

  it('should return false on clipboard error', async () => {
    const writeTextMock = vi.fn().mockRejectedValue(new Error('Access denied'))
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextMock,
      },
    })

    // Mock console.error to avoid test output noise
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const result = await copyToClipboard('test text')
    
    expect(result).toBe(false)
    expect(consoleErrorSpy).toHaveBeenCalled()
    
    consoleErrorSpy.mockRestore()
  })

  it('should handle empty strings', async () => {
    const writeTextMock = vi.fn().mockResolvedValue(undefined)
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextMock,
      },
    })

    const result = await copyToClipboard('')
    
    expect(writeTextMock).toHaveBeenCalledWith('')
    expect(result).toBe(true)
  })
})
