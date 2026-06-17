// admin-table.js — daisyUI 기반 공통 테이블 (Tabulator 대체)
class AdminTable {
  constructor(cfg) {
    this.cfg = Object.assign({ pageSize: 10 }, cfg);
    this.page = 0; // 0-based
    this.totalPages = 1;
    this.sortField = cfg.defaultSort?.field || 'createdDate';
    this.sortDir = cfg.defaultSort?.dir || 'desc';
    this._renderHead();
    this._bindForm();
  }

  _renderHead() {
    const thead = this.cfg.tableEl.querySelector('thead');
    thead.innerHTML = '<tr>' + this.cfg.columns.map((c, i) =>
      `<th class="${c.sortable ? 'cursor-pointer select-none' : ''}" data-col="${i}">${c.title}` +
      (c.sortable ? ' <span class="opacity-40">↕</span>' : '') + '</th>').join('') + '</tr>';
    if (this.cfg.columns.some(c => c.sortable)) {
      thead.querySelectorAll('th[data-col]').forEach(th => {
        const col = this.cfg.columns[+th.dataset.col];
        if (!col.sortable) return;
        th.addEventListener('click', () => {
          if (this.sortField === col.field) { this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc'; }
          else { this.sortField = col.field; this.sortDir = 'asc'; }
          this.goToPage(0);
        });
      });
    }
  }

  _bindForm() {
    if (!this.cfg.formEl) return;
    this.cfg.formEl.addEventListener('submit', (e) => { e.preventDefault(); this.reload(); });
  }

  reload() { this.goToPage(0); }

  goToPage(n) {
    this.page = n;
    const formData = this.cfg.formEl ? new FormData(this.cfg.formEl) : new FormData();
    formData.append('pageNumber', this.page);
    formData.append('pageSize', this.cfg.pageSize);
    formData.append('sortField', this.sortField);
    formData.append('sortDirection', this.sortDir);
    if (this.cfg.beforeSend) this.cfg.beforeSend(formData);
    adminFetch(this.cfg.url, formData)
      .then(res => this._render(res))
      .catch(err => { if (err.message !== 'unauthorized') showToast('조회 실패: ' + err, 'error'); });
  }

  _resolve(obj, path) { return path.split('.').reduce((o, k) => (o == null ? o : o[k]), obj); }

  _render(res) {
    const pageObj = res[this.cfg.pageKey] || {};
    const rows = pageObj.content || [];
    this.totalPages = pageObj.totalPages || 1;
    const total = pageObj.totalElements || 0;
    if (this.cfg.countEl) this.cfg.countEl.textContent = `총 ${total.toLocaleString()}건`;

    const tbody = this.cfg.tbodyEl;
    if (rows.length === 0) {
      tbody.innerHTML = `<tr><td colspan="${this.cfg.columns.length}" class="text-center text-base-content/50 py-12">
        <i data-lucide="inbox" class="size-10 mx-auto mb-2 opacity-40"></i><p>데이터가 없습니다.</p></td></tr>`;
    } else {
      tbody.innerHTML = rows.map((row, ri) => '<tr class="hover:bg-base-200' +
        (this.cfg.onRowClick ? ' cursor-pointer' : '') + `" data-ri="${ri}">` +
        this.cfg.columns.map(c => {
          let v = c.field ? this._resolve(row, c.field) : null;
          const cell = c.render ? c.render(v, row, this.page * this.cfg.pageSize + ri) : (v ?? '');
          return `<td class="text-sm"${c.width ? ` style="width:${c.width}"` : ''}>${cell}</td>`;
        }).join('') + '</tr>').join('');
      if (this.cfg.onRowClick) {
        tbody.querySelectorAll('tr[data-ri]').forEach(tr => {
          tr.addEventListener('click', () => this.cfg.onRowClick(rows[+tr.dataset.ri]));
        });
      }
    }
    this._renderPagination();
    lucide.createIcons();
  }

  _renderPagination() {
    if (!this.cfg.paginationEl) return;
    const cur = this.page, last = this.totalPages;
    const btn = (label, target, disabled, active) =>
      `<button class="join-item btn btn-sm ${active ? 'btn-active' : ''} ${disabled ? 'btn-disabled' : ''}" ${disabled ? 'disabled' : `data-page="${target}"`}>${label}</button>`;
    let html = '<div class="join">';
    html += btn('«', cur - 1, cur <= 0, false);
    html += btn(`${cur + 1} / ${last}`, cur, true, true);
    html += btn('»', cur + 1, cur >= last - 1, false);
    html += '</div>';
    this.cfg.paginationEl.innerHTML = html;
    this.cfg.paginationEl.querySelectorAll('button[data-page]').forEach(b => {
      b.addEventListener('click', () => this.goToPage(+b.dataset.page));
    });
  }
}
window.AdminTable = AdminTable;
