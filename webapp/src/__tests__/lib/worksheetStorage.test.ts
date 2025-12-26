import { describe, it, expect, beforeEach } from 'vitest';
import type { KVNamespace } from '@cloudflare/workers-types';
import {
  saveWorksheet,
  getWorksheet,
  listWorksheets,
  searchWorksheets,
  rateWorksheet,
  calculateWorksheetRatingStats,
} from '@/lib/server/worksheetStorage';

// Minimal in-memory KV mock
function createMockKV() {
  const store = new Map<string, string>();

  const kv: Partial<KVNamespace> = {
    async put(key: string, value: string) {
      store.set(key, value);
      return;
    },
    async get(key: string, type?: 'text' | 'json') {
      const v = store.get(key);
      if (v === undefined) return null;
      if (type === 'json') return JSON.parse(v);
      return v;
    },
    async list({ prefix }: { prefix?: string | undefined }) {
      const keys = Array.from(store.keys())
        .filter((k) => (prefix ? k.startsWith(prefix) : true))
        .map((name) => ({ name }));
      return { keys } as any;
    },
  };

  return { kv, store } as const;
}

function makeWorksheet(id: string, title: string, createdAt: string, views = 0, downloads = 0, avg = 0, count = 0) {
  return {
    id,
    type: 'explicit' as const,
    title,
    subtitle: `subtitle ${id}`,
    description: `desc ${id}`,
    grades: ['kindergarten', 'grade1', 'grade2'] as const,
    problems: [
      { operand1: 1, operand2: 1, operation: 'addition' },
    ],
    createdAt,
    stats: { views, downloads, averageRating: avg, ratingCount: count },
  };
}

describe('worksheetStorage - search, pagination, ratings', () => {
  let mock: ReturnType<typeof createMockKV>;
  let ctx: any;

  beforeEach(() => {
    mock = createMockKV();
    ctx = { env: { KV: mock.kv } };
  });

  it('listWorksheets returns sorted and paginated results', async () => {
    const w1 = makeWorksheet('a1', 'Addition Basics', '2025-12-01T00:00:00Z', 5, 1, 4.0, 1);
    const w2 = makeWorksheet('b2', 'Subtraction Fun', '2025-12-05T00:00:00Z', 10, 2, 4.5, 2);
    const w3 = makeWorksheet('c3', 'Multiply', '2025-11-30T00:00:00Z', 2, 0, 3.0, 1);

    await mock.kv.put(`worksheet:${w1.id}`, JSON.stringify(w1));
    await mock.kv.put(`worksheet:${w2.id}`, JSON.stringify(w2));
    await mock.kv.put(`worksheet:${w3.id}`, JSON.stringify(w3));

    const res = await listWorksheets(ctx, { sortBy: 'views', limit: 1, offset: 0 });
    expect(res.total).toBe(3);
    expect(res.items).toHaveLength(1);
    expect(res.items[0].id).toBe('b2'); // highest views
    expect(res.hasMore).toBe(true);

    const page2 = await listWorksheets(ctx, { sortBy: 'views', limit: 1, offset: 1 });
    expect(page2.items[0].id).toBe('a1');
  });

  it('searchWorksheets finds by title, subtitle, description and paginates', async () => {
    const w1 = makeWorksheet('a1', 'Addition Basics', '2025-12-01T00:00:00Z');
    const w2 = makeWorksheet('b2', 'Subtraction Fun', '2025-12-05T00:00:00Z');
    const w3 = makeWorksheet('c3', 'Advanced Addition', '2025-11-30T00:00:00Z');

    await mock.kv.put(`worksheet:${w1.id}`, JSON.stringify(w1));
    await mock.kv.put(`worksheet:${w2.id}`, JSON.stringify(w2));
    await mock.kv.put(`worksheet:${w3.id}`, JSON.stringify(w3));

    const res = await searchWorksheets(ctx, 'addition', { limit: 10, offset: 0 });
    expect(res.total).toBe(2);
    expect(res.items.map((i) => i.id).sort()).toEqual(['a1', 'c3'].sort());
  });

  it('rateWorksheet stores ratings and updates stats correctly', async () => {
    const w = makeWorksheet('a1', 'Addition Basics', '2025-12-01T00:00:00Z', 0, 0, 0, 0);
    await mock.kv.put(`worksheet:${w.id}`, JSON.stringify(w));

    // First rating
    const ok1 = await rateWorksheet(ctx, w.id, 5, 'sess1');
    expect(ok1).toBe(true);

    let stats = await calculateWorksheetRatingStats(ctx, w.id);
    expect(stats.ratingCount).toBe(1);
    expect(stats.averageRating).toBe(5);

    // Update same session rating to 3
    const ok2 = await rateWorksheet(ctx, w.id, 3, 'sess1');
    expect(ok2).toBe(true);

    stats = await calculateWorksheetRatingStats(ctx, w.id);
    expect(stats.ratingCount).toBe(1);
    expect(stats.averageRating).toBe(3);

    // Another user rates 4
    const ok3 = await rateWorksheet(ctx, w.id, 4, 'sess2');
    expect(ok3).toBe(true);

    stats = await calculateWorksheetRatingStats(ctx, w.id);
    expect(stats.ratingCount).toBe(2);
    expect(stats.averageRating).toBe(3.5);

    // Ensure worksheet saved stats updated
    const updated = await getWorksheet(ctx, w.id);
    expect(updated).not.toBeNull();
    expect(updated!.stats.ratingCount).toBe(2);
    expect(updated!.stats.averageRating).toBe(3.5);
  });

  it('rateWorksheet rejects invalid ratings', async () => {
    const w = makeWorksheet('x1', 'Test', '2025-12-01T00:00:00Z');
    await mock.kv.put(`worksheet:${w.id}`, JSON.stringify(w));

    const ok = await rateWorksheet(ctx, w.id, 0, 's1');
    expect(ok).toBe(false);
  });
});
