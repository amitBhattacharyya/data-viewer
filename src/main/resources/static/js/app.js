document.addEventListener('DOMContentLoaded', () => {
    const app = document.getElementById('app');
    const collapseToggle = document.getElementById('collapseToggle');

    if (collapseToggle) {
        collapseToggle.addEventListener('click', () => {
            const collapsed = app.classList.toggle('sidebar-collapsed');
            collapseToggle.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
            collapseToggle.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
        });
    }

    const sqlQuery = document.getElementById('sqlQuery');
    const runQueryBtn = document.getElementById('runQueryBtn');
    const errorBanner = document.getElementById('errorBanner');
    const execStats = document.getElementById('execStats');
    const rowCountBadge = document.getElementById('rowCountBadge');
    const timeBadge = document.getElementById('timeBadge');
    const columnsBadge = document.getElementById('columnsBadge');
    const datagrid = document.getElementById('datagrid');
    const gridHead = document.getElementById('gridHead');
    const gridBody = document.getElementById('gridBody');
    const emptyPlaceholder = document.getElementById('emptyPlaceholder');

    // Quick presets
    document.querySelectorAll('.quick-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            sqlQuery.value = btn.getAttribute('data-sql');
            executeQuery();
        });
    });

    // Run button & Ctrl+Enter shortcut
    if (runQueryBtn) {
        runQueryBtn.addEventListener('click', executeQuery);
    }

    if (sqlQuery) {
        sqlQuery.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                executeQuery();
            }
        });
    }

    async function executeQuery() {
        const query = sqlQuery.value.trim();
        if (!query) {
            showError('Please enter a SQL query.');
            return;
        }

        clearError();
        setLoading(true);

        try {
            const response = await fetch('/api/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ query: query })
            });

            if (!response.ok) {
                throw new Error(`HTTP error ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();

            if (!data.success) {
                showError(data.error || 'Failed to execute query.');
                renderEmpty();
            } else {
                renderGrid(data.columns, data.rows);
                rowCountBadge.textContent = `${data.rowCount} rows`;
                timeBadge.textContent = `${data.executionTimeMs} ms`;
                columnsBadge.textContent = `${data.columns ? data.columns.length : 0} columns`;
                execStats.style.display = 'inline-flex';
            }
        } catch (err) {
            showError(err.message || 'Error executing query.');
            renderEmpty();
        } finally {
            setLoading(false);
        }
    }

    function renderGrid(columns, rows) {
        gridHead.innerHTML = '';
        gridBody.innerHTML = '';

        if (!columns || columns.length === 0) {
            renderEmpty('Query executed successfully, but returned no columns.');
            return;
        }

        // Render dynamic header based on query output
        const headerRow = document.createElement('tr');
        columns.forEach(col => {
            const th = document.createElement('th');
            th.textContent = col;
            headerRow.appendChild(th);
        });
        gridHead.appendChild(headerRow);

        // Render dynamic rows
        if (!rows || rows.length === 0) {
            emptyPlaceholder.textContent = 'Query returned 0 rows.';
            emptyPlaceholder.style.display = 'block';
            datagrid.style.display = 'table';
            return;
        }

        emptyPlaceholder.style.display = 'none';
        datagrid.style.display = 'table';

        rows.forEach(row => {
            const tr = document.createElement('tr');
            columns.forEach(col => {
                const td = document.createElement('td');
                const val = row[col];

                if (val === null || val === undefined) {
                    td.innerHTML = '<span style="color:#94a3b8;font-style:italic;">NULL</span>';
                } else {
                    const strVal = String(val);
                    // If JSON string, format or style nicely
                    if ((strVal.startsWith('{') && strVal.endsWith('}')) || (strVal.startsWith('[') && strVal.endsWith(']'))) {
                        const span = document.createElement('span');
                        span.className = 'json-cell';
                        span.textContent = strVal;
                        span.title = 'Click to copy JSON';
                        span.addEventListener('click', () => {
                            navigator.clipboard.writeText(strVal);
                            const orig = span.textContent;
                            span.textContent = 'Copied to clipboard!';
                            setTimeout(() => { span.textContent = orig; }, 1200);
                        });
                        td.appendChild(span);
                    } else {
                        td.textContent = strVal;
                        td.title = strVal;
                    }
                }
                tr.appendChild(td);
            });
            gridBody.appendChild(tr);
        });
    }

    function renderEmpty(msg = 'No data to display.') {
        gridHead.innerHTML = '';
        gridBody.innerHTML = '';
        datagrid.style.display = 'none';
        emptyPlaceholder.textContent = msg;
        emptyPlaceholder.style.display = 'block';
        execStats.style.display = 'none';
    }

    function showError(msg) {
        errorBanner.textContent = msg;
        errorBanner.style.display = 'block';
    }

    function clearError() {
        errorBanner.textContent = '';
        errorBanner.style.display = 'none';
    }

    function setLoading(isLoading) {
        runQueryBtn.disabled = isLoading;
        if (isLoading) {
            runQueryBtn.querySelector('span').textContent = 'Executing...';
        } else {
            runQueryBtn.querySelector('span').textContent = 'Execute Query';
        }
    }

    // Auto-run default query on page load
    executeQuery();
});
