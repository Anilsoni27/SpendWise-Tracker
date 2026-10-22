/**
 * SpendWise — Front-End Application Logic
 */

(function () {
    'use strict';

    // Currency Configurations
    const CURRENCIES = {
        'INR': { code: 'INR', symbol: '₹', locale: 'en-IN', name: 'INR (₹)', decimals: 2 },
        'USD': { code: 'USD', symbol: '$', locale: 'en-US', name: 'USD ($)', decimals: 2 },
        'EUR': { code: 'EUR', symbol: '€', locale: 'en-IE', name: 'EUR (€)', decimals: 2 },
        'GBP': { code: 'GBP', symbol: '£', locale: 'en-GB', name: 'GBP (£)', decimals: 2 },
        'JPY': { code: 'JPY', symbol: '¥', locale: 'ja-JP', name: 'JPY (¥)', decimals: 0 },
        'CAD': { code: 'CAD', symbol: 'CA$', locale: 'en-CA', name: 'CAD (CA$)', decimals: 2 },
        'AUD': { code: 'AUD', symbol: 'A$', locale: 'en-AU', name: 'AUD (A$)', decimals: 2 },
        'AED': { code: 'AED', symbol: 'AED ', locale: 'en-AE', name: 'AED', decimals: 2 },
        'SGD': { code: 'SGD', symbol: 'SG$', locale: 'en-SG', name: 'SGD (SG$)', decimals: 2 },
        'CHF': { code: 'CHF', symbol: 'CHF ', locale: 'de-CH', name: 'CHF', decimals: 2 }
    };

    // Conversion rates relative to USD (1 USD = X Currency)
    const RATES_TO_USD = {
        'USD': 1.0,
        'INR': 83.5,
        'EUR': 0.92,
        'GBP': 0.79,
        'JPY': 150.0,
        'CAD': 1.36,
        'AUD': 1.52,
        'AED': 3.67,
        'SGD': 1.35,
        'CHF': 0.89
    };

    function convertAmount(amount, fromCur, toCur) {
        const val = Number(amount) || 0;
        const from = (fromCur || 'INR').toUpperCase();
        const to = (toCur || 'INR').toUpperCase();
        if (from === to) return val;

        const fromRate = RATES_TO_USD[from] || 1.0;
        const toRate = RATES_TO_USD[to] || 1.0;
        // Convert to USD base then to target currency
        const inUsd = val / fromRate;
        return inUsd * toRate;
    }

    function getCurrencyConfig(code) {
        const c = (code || state.currentCurrency || 'INR').toUpperCase();
        return CURRENCIES[c] || CURRENCIES['INR'];
    }

    // State
    const state = {
        categories: [],
        expenses: [],
        categoryMap: {},
        categoryChartInstance: null,
        dailyChartInstance: null,
        deleteTargetId: null,
        searchDebounceTimer: null,
        currentCurrency: localStorage.getItem('spendwise_currency') || 'INR',
        theme: localStorage.getItem('spendwise_theme') || (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'),
        isFiltered: false
    };

    // DOM Elements
    const elements = {
        themeToggleBtn: document.getElementById('themeToggleBtn'),
        currencySelector: document.getElementById('currencySelector'),
        currentDateDisplay: document.getElementById('currentDateDisplay'),
        statTodayTotal: document.getElementById('statTodayTotal'),
        statYesterdaySub: document.getElementById('statYesterdaySub'),
        statMonthTotal: document.getElementById('statMonthTotal'),
        statLastMonthSub: document.getElementById('statLastMonthSub'),
        statAvgExpense: document.getElementById('statAvgExpense'),
        statTotalCountSub: document.getElementById('statTotalCountSub'),
        statTopCategory: document.getElementById('statTopCategory'),
        statTopCategorySub: document.getElementById('statTopCategorySub'),

        categoryLegend: document.getElementById('categoryLegend'),

        filterSearch: document.getElementById('filterSearch'),
        filterCategory: document.getElementById('filterCategory'),
        filterDateRange: document.getElementById('filterDateRange'),
        customDateRangeInputs: document.getElementById('customDateRangeInputs'),
        filterStartDate: document.getElementById('filterStartDate'),
        filterEndDate: document.getElementById('filterEndDate'),
        filterSort: document.getElementById('filterSort'),
        btnClearFilters: document.getElementById('btnClearFilters'),

        expensesTable: document.getElementById('expensesTable'),
        expensesTableBody: document.getElementById('expensesTableBody'),
        emptyState: document.getElementById('emptyState'),
        tableFilterSummary: document.getElementById('tableFilterSummary'),
        footerRecordCount: document.getElementById('footerRecordCount'),
        footerFilteredTotal: document.getElementById('footerFilteredTotal'),

        btnOpenAddModal: document.getElementById('btnOpenAddModal'),
        btnExportCsv: document.getElementById('btnExportCsv'),
        btnResetDemo: document.getElementById('btnResetDemo'),
        btnEmptyReset: document.getElementById('btnEmptyReset'),

        // Modal Elements
        expenseModal: document.getElementById('expenseModal'),
        modalTitle: document.getElementById('modalTitle'),
        btnModalClose: document.getElementById('btnModalClose'),
        btnModalCancel: document.getElementById('btnModalCancel'),
        expenseForm: document.getElementById('expenseForm'),
        formExpenseId: document.getElementById('formExpenseId'),
        formTitle: document.getElementById('formTitle'),
        formCurrency: document.getElementById('formCurrency'),
        formAmount: document.getElementById('formAmount'),
        formAmountLabel: document.getElementById('formAmountLabel'),
        formCategory: document.getElementById('formCategory'),
        formDate: document.getElementById('formDate'),
        formPaymentMethod: document.getElementById('formPaymentMethod'),
        formNotes: document.getElementById('formNotes'),

        // Error elements
        errorTitle: document.getElementById('errorTitle'),
        errorCurrency: document.getElementById('errorCurrency'),
        errorAmount: document.getElementById('errorAmount'),
        errorCategory: document.getElementById('errorCategory'),
        errorDate: document.getElementById('errorDate'),
        errorPaymentMethod: document.getElementById('errorPaymentMethod'),
        errorNotes: document.getElementById('errorNotes'),

        // Delete Modal
        deleteModal: document.getElementById('deleteModal'),
        deleteExpenseTitle: document.getElementById('deleteExpenseTitle'),
        btnDeleteModalClose: document.getElementById('btnDeleteModalClose'),
        btnCancelDelete: document.getElementById('btnCancelDelete'),
        btnConfirmDelete: document.getElementById('btnConfirmDelete'),

        toastContainer: document.getElementById('toastContainer')
    };

    // Initialize
    document.addEventListener('DOMContentLoaded', init);

    async function init() {
        initTheme();
        const user = await checkAuth();
        if (!user) return;
        displayTodayDate();
        initCurrencyPreference();
        setupUserMenu(user);
        setupEventListeners();
        await loadCategories();
        await refreshAllData();
    }

    async function checkAuth() {
        try {
            const res = await fetch('/api/auth/me');
            if (!res.ok) {
                window.location.replace('/login.html');
                return null;
            }
            const user = await res.json();
            return user;
        } catch (e) {
            window.location.replace('/login.html');
            return null;
        }
    }

    function setupUserMenu(user) {
        if (!user) return;
        const userFullName = document.getElementById('userFullName');
        const userUsername = document.getElementById('userUsername');
        const userAvatar = document.getElementById('userAvatar');
        const btnLogout = document.getElementById('btnLogout');

        if (userFullName) userFullName.textContent = user.fullName || user.username;
        if (userUsername) userUsername.textContent = '@' + user.username;
        if (userAvatar) {
            const initial = (user.fullName || user.username || 'U').trim().charAt(0).toUpperCase();
            userAvatar.textContent = initial;
        }

        if (btnLogout) {
            btnLogout.addEventListener('click', async () => {
                try {
                    await fetch('/api/auth/logout', { method: 'POST' });
                } catch(e) {}
                window.location.replace('/login.html');
            });
        }
    }

    function initTheme() {
        const savedTheme = localStorage.getItem('spendwise_theme');
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        state.theme = savedTheme || (prefersDark ? 'dark' : 'light');
        document.documentElement.setAttribute('data-theme', state.theme);

        if (elements.themeToggleBtn) {
            elements.themeToggleBtn.addEventListener('click', toggleTheme);
        }

        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (!localStorage.getItem('spendwise_theme')) {
                    state.theme = e.matches ? 'dark' : 'light';
                    document.documentElement.setAttribute('data-theme', state.theme);
                    loadCharts();
                }
            });
        }
    }

    function toggleTheme() {
        const newTheme = state.theme === 'dark' ? 'light' : 'dark';
        state.theme = newTheme;
        localStorage.setItem('spendwise_theme', newTheme);
        document.documentElement.setAttribute('data-theme', newTheme);
        loadCharts();
        showToast(newTheme === 'dark' ? 'Dark mode enabled 🌙' : 'Light mode enabled ☀️');
    }

    function displayTodayDate() {
        const now = new Date();
        const options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
        elements.currentDateDisplay.textContent = now.toLocaleDateString('en-US', options);
    }

    function initCurrencyPreference() {
        if (elements.currencySelector) {
            elements.currencySelector.value = state.currentCurrency;
            elements.currencySelector.addEventListener('change', handleGlobalCurrencyChange);
        }
        if (elements.formCurrency) {
            elements.formCurrency.value = state.currentCurrency;
            elements.formCurrency.addEventListener('change', handleFormCurrencyChange);
        }
        updateFormAmountLabel(state.currentCurrency);
    }

    function updateFormAmountLabel(currencyCode) {
        if (elements.formAmountLabel) {
            const sym = getCurrencyConfig(currencyCode).symbol.trim();
            elements.formAmountLabel.textContent = `Amount (${sym})`;
        }
    }

    function handleGlobalCurrencyChange(e) {
        state.currentCurrency = e.target.value;
        localStorage.setItem('spendwise_currency', state.currentCurrency);
        if (elements.formCurrency) {
            elements.formCurrency.value = state.currentCurrency;
        }
        updateFormAmountLabel(state.currentCurrency);
        loadSummary();
        loadCharts();
        renderExpensesTable(state.expenses, state.isFiltered);
        showToast(`Currency view changed to ${getCurrencyConfig().name}`);
    }

    function handleFormCurrencyChange(e) {
        updateFormAmountLabel(e.target.value);
    }

    function setupEventListeners() {
        // Modal openers
        elements.btnOpenAddModal.addEventListener('click', openAddModal);
        elements.btnModalClose.addEventListener('click', closeExpenseModal);
        elements.btnModalCancel.addEventListener('click', closeExpenseModal);

        // Delete Modal
        elements.btnDeleteModalClose.addEventListener('click', closeDeleteModal);
        elements.btnCancelDelete.addEventListener('click', closeDeleteModal);
        elements.btnConfirmDelete.addEventListener('click', confirmDeleteExpense);

        // Form submit
        elements.expenseForm.addEventListener('submit', handleFormSubmit);

        // Filters
        elements.filterSearch.addEventListener('input', () => {
            clearTimeout(state.searchDebounceTimer);
            state.searchDebounceTimer = setTimeout(loadExpenses, 250);
        });

        elements.filterCategory.addEventListener('change', loadExpenses);

        elements.filterDateRange.addEventListener('change', handleDateRangeChange);
        elements.filterStartDate.addEventListener('change', loadExpenses);
        elements.filterEndDate.addEventListener('change', loadExpenses);
        elements.filterSort.addEventListener('change', loadExpenses);

        elements.btnClearFilters.addEventListener('click', resetFilters);
        elements.btnEmptyReset.addEventListener('click', resetFilters);

        // Export and Reset Demo
        elements.btnExportCsv.addEventListener('click', handleExportCsv);
        elements.btnResetDemo.addEventListener('click', handleResetDemo);

        // Close modal on Escape
        window.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeExpenseModal();
                closeDeleteModal();
            }
        });

        // Close on outside backdrop click
        elements.expenseModal.addEventListener('click', (e) => {
            if (e.target === elements.expenseModal) closeExpenseModal();
        });
        elements.deleteModal.addEventListener('click', (e) => {
            if (e.target === elements.deleteModal) closeDeleteModal();
        });
    }

    // API Helpers
    async function apiFetch(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(options.headers || {})
                },
                ...options
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.replace('/login.html');
                    return null;
                }
                let errData;
                try {
                    errData = await response.json();
                } catch (_) {
                    errData = { message: `Request failed with status ${response.status}` };
                }
                throw errData;
            }

            if (response.status === 204) return null;
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // Refresh everything
    async function refreshAllData() {
        await Promise.all([
            loadSummary(),
            loadCharts(),
            loadExpenses()
        ]);
    }

    // Categories
    async function loadCategories() {
        try {
            const categories = await apiFetch('/api/expenses/categories');
            state.categories = categories;

            // Populate categoryMap
            categories.forEach(c => {
                state.categoryMap[c.id] = c;
            });

            // Populate filter select
            elements.filterCategory.innerHTML = '<option value="">All Categories</option>';
            // Populate form select
            elements.formCategory.innerHTML = '<option value="" disabled selected>Select category</option>';

            categories.forEach(cat => {
                const optFilter = document.createElement('option');
                optFilter.value = cat.id;
                optFilter.textContent = cat.displayName;
                elements.filterCategory.appendChild(optFilter);

                const optForm = document.createElement('option');
                optForm.value = cat.id;
                optForm.textContent = cat.displayName;
                elements.formCategory.appendChild(optForm);
            });
        } catch (err) {
            showToast('Failed to load categories', true);
        }
    }

    // Summary Cards (Calculated and formatted in user's preferred currency)
    async function loadSummary() {
        try {
            const summary = await apiFetch('/api/expenses/summary');
            const targetCur = state.currentCurrency;

            // If user has expenses with mixed currencies or converted values:
            const todayAmt = convertAmount(summary.todayTotal, 'INR', targetCur);
            const yesterdayAmt = convertAmount(summary.yesterdayTotal, 'INR', targetCur);
            const monthAmt = convertAmount(summary.thisMonthTotal, 'INR', targetCur);
            const lastMonthAmt = convertAmount(summary.lastMonthTotal, 'INR', targetCur);
            const avgAmt = convertAmount(summary.averageExpense, 'INR', targetCur);
            const topAmt = convertAmount(summary.topCategoryAmount, 'INR', targetCur);

            elements.statTodayTotal.textContent = formatCurrency(todayAmt, targetCur);
            elements.statYesterdaySub.textContent = `Yesterday: ${formatCurrency(yesterdayAmt, targetCur)}`;

            elements.statMonthTotal.textContent = formatCurrency(monthAmt, targetCur);
            elements.statLastMonthSub.textContent = `Last month: ${formatCurrency(lastMonthAmt, targetCur)}`;

            elements.statAvgExpense.textContent = formatCurrency(avgAmt, targetCur);
            elements.statTotalCountSub.textContent = `${summary.totalCount} total recorded`;

            elements.statTopCategory.textContent = summary.topCategory || 'None';
            elements.statTopCategorySub.textContent = topAmt > 0
                ? `${formatCurrency(topAmt, targetCur)} this month`
                : 'No expenses yet';
        } catch (err) {
            console.error('Error loading summary:', err);
        }
    }

    // Date Range Presets
    function handleDateRangeChange() {
        const val = elements.filterDateRange.value;
        const now = new Date();
        const y = now.getFullYear();
        const m = now.getMonth();
        const d = now.getDate();

        if (val === 'custom') {
            elements.customDateRangeInputs.style.display = 'inline-flex';
            return;
        } else {
            elements.customDateRangeInputs.style.display = 'none';
        }

        if (val === 'today') {
            const todayStr = formatDateIso(now);
            elements.filterStartDate.value = todayStr;
            elements.filterEndDate.value = todayStr;
        } else if (val === 'this_week') {
            const dayOfWeek = now.getDay(); // 0 is Sunday
            const startOfWeek = new Date(now);
            startOfWeek.setDate(d - (dayOfWeek === 0 ? 6 : dayOfWeek - 1)); // Monday as start
            elements.filterStartDate.value = formatDateIso(startOfWeek);
            elements.filterEndDate.value = formatDateIso(now);
        } else if (val === 'this_month') {
            const startOfMonth = new Date(y, m, 1);
            const endOfMonth = new Date(y, m + 1, 0);
            elements.filterStartDate.value = formatDateIso(startOfMonth);
            elements.filterEndDate.value = formatDateIso(endOfMonth);
        } else if (val === 'last_month') {
            const startOfLast = new Date(y, m - 1, 1);
            const endOfLast = new Date(y, m, 0);
            elements.filterStartDate.value = formatDateIso(startOfLast);
            elements.filterEndDate.value = formatDateIso(endOfLast);
        } else {
            elements.filterStartDate.value = '';
            elements.filterEndDate.value = '';
        }

        loadExpenses();
    }

    // Load Expenses Table
    async function loadExpenses() {
        const params = new URLSearchParams();

        const search = elements.filterSearch.value.trim();
        if (search) params.append('search', search);

        const category = elements.filterCategory.value;
        if (category) params.append('category', category);

        const startDate = elements.filterStartDate.value;
        if (startDate) params.append('startDate', startDate);

        const endDate = elements.filterEndDate.value;
        if (endDate) params.append('endDate', endDate);

        const [sortBy, sortDir] = elements.filterSort.value.split(':');
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);

        // Update clear filters button visibility
        const isFiltered = Boolean(search || category || startDate || endDate || elements.filterDateRange.value !== 'all');
        elements.btnClearFilters.style.display = isFiltered ? 'inline-block' : 'none';

        try {
            const expenses = await apiFetch(`/api/expenses?${params.toString()}`);
            state.expenses = expenses;
            renderExpensesTable(expenses, isFiltered);
        } catch (err) {
            showToast('Failed to load expenses list', true);
        }
    }

    function renderExpensesTable(expenses, isFiltered) {
        state.isFiltered = Boolean(isFiltered);
        elements.expensesTableBody.innerHTML = '';

        let filteredSumConverted = 0;
        const targetCur = state.currentCurrency;

        if (expenses.length === 0) {
            elements.expensesTable.style.display = 'none';
            elements.emptyState.style.display = 'block';
            elements.footerRecordCount.textContent = '0 expenses';
            elements.footerFilteredTotal.textContent = formatCurrency(0, targetCur);
            elements.tableFilterSummary.textContent = isFiltered ? 'No matching expenses' : 'No expenses recorded yet';
            return;
        }

        elements.expensesTable.style.display = 'table';
        elements.emptyState.style.display = 'none';

        expenses.forEach(exp => {
            const expCur = exp.currency || 'INR';
            // Convert to selected base currency for sum
            const convertedVal = convertAmount(exp.amount, expCur, targetCur);
            filteredSumConverted += convertedVal;

            const tr = document.createElement('tr');

            const catInfo = state.categoryMap[exp.category] || {
                displayName: exp.category,
                color: '#64748b'
            };

            // Display in expense's own currency, and if different from viewing currency, show equivalent
            const ownCurrencyDisplay = formatCurrency(exp.amount, expCur);
            const conversionSubtext = (expCur !== targetCur)
                ? `<div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">≈ ${formatCurrency(convertedVal, targetCur)}</div>`
                : '';

            tr.innerHTML = `
                <td class="expense-date">${formatDateDisplay(exp.expenseDate)}</td>
                <td>
                    <div class="expense-title">${escapeHtml(exp.title)}</div>
                    ${exp.notes ? `<div class="expense-notes">${escapeHtml(exp.notes)}</div>` : ''}
                </td>
                <td>
                    <span class="category-badge">
                        <span class="category-badge-dot" style="background-color: ${catInfo.color}"></span>
                        ${escapeHtml(catInfo.displayName)}
                    </span>
                </td>
                <td class="expense-payment">${escapeHtml(exp.paymentMethod || '—')}</td>
                <td class="text-right">
                    <span class="expense-amount">${ownCurrencyDisplay}</span>
                    ${conversionSubtext}
                </td>
                <td class="text-right">
                    <div class="action-buttons">
                        <button class="btn btn-secondary btn-icon" data-action="edit" data-id="${exp.id}" title="Edit expense">
                            Edit
                        </button>
                        <button class="btn btn-secondary btn-icon btn-icon-danger" data-action="delete" data-id="${exp.id}" title="Delete expense">
                            Delete
                        </button>
                    </div>
                </td>
            `;

            // Attach action listeners
            const btnEdit = tr.querySelector('[data-action="edit"]');
            const btnDelete = tr.querySelector('[data-action="delete"]');

            btnEdit.addEventListener('click', () => openEditModal(exp));
            btnDelete.addEventListener('click', () => openDeleteModal(exp));

            elements.expensesTableBody.appendChild(tr);
        });

        // Summary footer
        elements.footerRecordCount.textContent = `${expenses.length} ${expenses.length === 1 ? 'expense' : 'expenses'}`;
        elements.footerFilteredTotal.textContent = formatCurrency(filteredSumConverted, targetCur);
        elements.tableFilterSummary.textContent = isFiltered
            ? `Filtered: ${expenses.length} records matching criteria`
            : `Showing all ${expenses.length} recorded expenses`;
    }

    // Charts
    async function loadCharts() {
        await Promise.all([
            loadCategoryChart(),
            loadDailyChart()
        ]);
    }

    async function loadCategoryChart() {
        try {
            const data = await apiFetch('/api/expenses/charts/categories');
            const targetCur = state.currentCurrency;

            // Render custom legend
            elements.categoryLegend.innerHTML = '';
            if (data.length === 0) {
                elements.categoryLegend.innerHTML = '<div style="color: var(--text-muted); font-size: 0.8rem; text-align: center; padding: 10px;">No expense data yet</div>';
            } else {
                data.forEach(item => {
                    const convertedItemAmount = convertAmount(item.totalAmount, 'INR', targetCur);
                    const row = document.createElement('div');
                    row.className = 'legend-item';
                    row.innerHTML = `
                        <div class="legend-left">
                            <span class="legend-dot" style="background-color: ${item.color}"></span>
                            <span>${escapeHtml(item.displayName)}</span>
                        </div>
                        <div class="legend-right">
                            ${formatCurrency(convertedItemAmount, targetCur)} (${item.percentage}%)
                        </div>
                    `;
                    elements.categoryLegend.appendChild(row);
                });
            }

            // Render Chart.js
            if (typeof Chart === 'undefined') return;

            const canvas = document.getElementById('categoryChart');
            const ctx = canvas.getContext('2d');

            if (state.categoryChartInstance) {
                state.categoryChartInstance.destroy();
            }

            const labels = data.map(d => d.displayName);
            const amounts = data.map(d => convertAmount(d.totalAmount, 'INR', targetCur));
            const colors = data.map(d => d.color);

            if (data.length === 0) {
                labels.push('No data');
                amounts.push(1);
                colors.push('#e2e8f0');
            }

            const isDark = (document.documentElement.getAttribute('data-theme') === 'dark');
            const donutBorder = isDark ? '#1E293B' : '#ffffff';

            state.categoryChartInstance = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: amounts,
                        backgroundColor: colors,
                        borderWidth: 2,
                        borderColor: donutBorder,
                        hoverOffset: 3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '68%',
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            backgroundColor: isDark ? '#1F2937' : '#0F172A',
                            titleColor: '#F8FAFC',
                            bodyColor: '#F8FAFC',
                            borderColor: isDark ? '#2D3748' : '#334155',
                            borderWidth: 1,
                            callbacks: {
                                label: function (context) {
                                    if (data.length === 0) return ' No expenses';
                                    const val = context.raw;
                                    return ` ${formatCurrency(val, targetCur)}`;
                                }
                            }
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Error loading category chart:', err);
        }
    }

    async function loadDailyChart() {
        try {
            const data = await apiFetch('/api/expenses/charts/daily?days=14');
            if (typeof Chart === 'undefined') return;

            const targetCur = state.currentCurrency;
            const canvas = document.getElementById('dailyChart');
            const ctx = canvas.getContext('2d');

            if (state.dailyChartInstance) {
                state.dailyChartInstance.destroy();
            }

            const labels = data.map(d => d.label);
            const amounts = data.map(d => convertAmount(d.amount, 'INR', targetCur));

            const isDark = (document.documentElement.getAttribute('data-theme') === 'dark');
            const barBg = isDark ? '#A3E635' : '#0F172A';
            const barHover = isDark ? '#BEF264' : '#A3E635';
            const gridColor = isDark ? '#334155' : '#F1F5F9';
            const tickColor = isDark ? '#94A3B8' : '#64748B';

            state.dailyChartInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Daily Spending',
                        data: amounts,
                        backgroundColor: barBg,
                        hoverBackgroundColor: barHover,
                        borderRadius: 4,
                        maxBarThickness: 28
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            backgroundColor: isDark ? '#162032' : '#0F172A',
                            titleColor: '#F8FAFC',
                            bodyColor: '#F8FAFC',
                            borderColor: isDark ? '#334155' : '#334155',
                            borderWidth: 1,
                            callbacks: {
                                label: function (context) {
                                    return ` Total: ${formatCurrency(context.raw, targetCur)}`;
                                }
                            }
                        }
                    },
                    scales: {
                        x: {
                            grid: {
                                display: false
                            },
                            ticks: {
                                font: {
                                    size: 11
                                },
                                color: tickColor
                            }
                        },
                        y: {
                            beginAtZero: true,
                            grid: {
                                color: gridColor
                            },
                            ticks: {
                                font: {
                                    size: 11
                                },
                                color: tickColor,
                                callback: function (value) {
                                    return getCurrencyConfig(targetCur).symbol + value;
                                }
                            }
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Error loading daily chart:', err);
        }
    }

    // Modal Operations
    function openAddModal() {
        clearFormErrors();
        elements.modalTitle.textContent = 'Add Expense';
        elements.formExpenseId.value = '';
        elements.expenseForm.reset();
        elements.formCurrency.value = state.currentCurrency;
        updateFormAmountLabel(state.currentCurrency);
        elements.formDate.value = formatDateIso(new Date());
        elements.formPaymentMethod.value = 'UPI / Wallet';

        elements.expenseModal.classList.add('is-open');
        elements.expenseModal.setAttribute('aria-hidden', 'false');
        setTimeout(() => elements.formTitle.focus(), 50);
    }

    function openEditModal(expense) {
        clearFormErrors();
        elements.modalTitle.textContent = 'Edit Expense';
        elements.formExpenseId.value = expense.id;
        elements.formTitle.value = expense.title;
        elements.formCurrency.value = expense.currency || state.currentCurrency || 'INR';
        updateFormAmountLabel(elements.formCurrency.value);
        elements.formAmount.value = expense.amount;
        elements.formCategory.value = expense.category;
        elements.formDate.value = expense.expenseDate;
        elements.formPaymentMethod.value = expense.paymentMethod;
        elements.formNotes.value = expense.notes || '';

        elements.expenseModal.classList.add('is-open');
        elements.expenseModal.setAttribute('aria-hidden', 'false');
        setTimeout(() => elements.formTitle.focus(), 50);
    }

    function closeExpenseModal() {
        elements.expenseModal.classList.remove('is-open');
        elements.expenseModal.setAttribute('aria-hidden', 'true');
        clearFormErrors();
    }

    function clearFormErrors() {
        elements.errorTitle.textContent = '';
        if (elements.errorCurrency) elements.errorCurrency.textContent = '';
        elements.errorAmount.textContent = '';
        elements.errorCategory.textContent = '';
        elements.errorDate.textContent = '';
        elements.errorPaymentMethod.textContent = '';
        elements.errorNotes.textContent = '';
    }

    async function handleFormSubmit(e) {
        e.preventDefault();
        clearFormErrors();

        const id = elements.formExpenseId.value;
        const title = elements.formTitle.value.trim();
        const currency = elements.formCurrency ? elements.formCurrency.value : state.currentCurrency;
        const amountStr = elements.formAmount.value;
        const category = elements.formCategory.value;
        const expenseDate = elements.formDate.value;
        const paymentMethod = elements.formPaymentMethod.value;
        const notes = elements.formNotes.value.trim();

        let hasError = false;

        if (!title) {
            elements.errorTitle.textContent = 'Please enter an expense title';
            hasError = true;
        }

        const amountNum = parseFloat(amountStr);
        if (isNaN(amountNum) || amountNum <= 0) {
            elements.errorAmount.textContent = 'Please enter a valid amount greater than ' + formatCurrency(0, currency);
            hasError = true;
        }

        if (!currency) {
            if (elements.errorCurrency) elements.errorCurrency.textContent = 'Please choose a currency';
            hasError = true;
        }

        if (!category) {
            elements.errorCategory.textContent = 'Please select a category';
            hasError = true;
        }

        if (!expenseDate) {
            elements.errorDate.textContent = 'Please choose a date';
            hasError = true;
        }

        if (!paymentMethod) {
            elements.errorPaymentMethod.textContent = 'Please select a payment method';
            hasError = true;
        }

        if (hasError) return;

        const payload = {
            title,
            amount: amountNum,
            currency: currency.toUpperCase(),
            category,
            expenseDate,
            paymentMethod,
            notes: notes || null
        };

        const isEdit = Boolean(id);
        const url = isEdit ? `/api/expenses/${id}` : '/api/expenses';
        const method = isEdit ? 'PUT' : 'POST';

        try {
            await apiFetch(url, {
                method,
                body: JSON.stringify(payload)
            });

            closeExpenseModal();
            showToast(isEdit ? 'Expense updated successfully' : 'Expense added successfully');
            await refreshAllData();
        } catch (err) {
            if (err.fieldErrors) {
                if (err.fieldErrors.title) elements.errorTitle.textContent = err.fieldErrors.title;
                if (err.fieldErrors.currency && elements.errorCurrency) elements.errorCurrency.textContent = err.fieldErrors.currency;
                if (err.fieldErrors.amount) elements.errorAmount.textContent = err.fieldErrors.amount;
                if (err.fieldErrors.category) elements.errorCategory.textContent = err.fieldErrors.category;
                if (err.fieldErrors.expenseDate) elements.errorDate.textContent = err.fieldErrors.expenseDate;
                if (err.fieldErrors.paymentMethod) elements.errorPaymentMethod.textContent = err.fieldErrors.paymentMethod;
            } else {
                showToast(err.message || 'Error saving expense', true);
            }
        }
    }

    // Delete Operations
    function openDeleteModal(expense) {
        state.deleteTargetId = expense.id;
        const cur = expense.currency || 'INR';
        elements.deleteExpenseTitle.textContent = `"${expense.title}" (${formatCurrency(expense.amount, cur)})`;
        elements.deleteModal.classList.add('is-open');
        elements.deleteModal.setAttribute('aria-hidden', 'false');
    }

    function closeDeleteModal() {
        state.deleteTargetId = null;
        elements.deleteModal.classList.remove('is-open');
        elements.deleteModal.setAttribute('aria-hidden', 'true');
    }

    async function confirmDeleteExpense() {
        if (!state.deleteTargetId) return;

        const id = state.deleteTargetId;
        try {
            await apiFetch(`/api/expenses/${id}`, { method: 'DELETE' });
            closeDeleteModal();
            showToast('Expense deleted');
            await refreshAllData();
        } catch (err) {
            showToast(err.message || 'Failed to delete expense', true);
        }
    }

    // Reset Demo Data
    async function handleResetDemo() {
        if (!confirm('Reset database to sample expense records? Any custom entries will be replaced.')) {
            return;
        }
        try {
            await apiFetch('/api/expenses/reset-demo', { method: 'POST' });
            showToast('Sample data reset successfully');
            await refreshAllData();
        } catch (err) {
            showToast('Failed to reset sample data', true);
        }
    }

    // Export CSV
    function handleExportCsv() {
        const params = new URLSearchParams();
        const search = elements.filterSearch.value.trim();
        if (search) params.append('search', search);

        const category = elements.filterCategory.value;
        if (category) params.append('category', category);

        const startDate = elements.filterStartDate.value;
        if (startDate) params.append('startDate', startDate);

        const endDate = elements.filterEndDate.value;
        if (endDate) params.append('endDate', endDate);

        window.location.href = `/api/expenses/export?${params.toString()}`;
        showToast('Exporting CSV file...');
    }

    // Reset Filters
    function resetFilters() {
        elements.filterSearch.value = '';
        elements.filterCategory.value = '';
        elements.filterDateRange.value = 'all';
        elements.customDateRangeInputs.style.display = 'none';
        elements.filterStartDate.value = '';
        elements.filterEndDate.value = '';
        elements.filterSort.value = 'expenseDate:desc';
        loadExpenses();
    }

    // Formatting & Helpers
    function formatCurrency(val, currencyCode) {
        const num = Number(val) || 0;
        const code = (currencyCode || state.currentCurrency || 'INR').toUpperCase();
        const config = CURRENCIES[code] || CURRENCIES['INR'];
        const decimals = config.decimals !== undefined ? config.decimals : 2;
        return config.symbol + num.toLocaleString(config.locale, {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    function formatDateDisplay(isoString) {
        if (!isoString) return '—';
        const [y, m, d] = isoString.split('-');
        const date = new Date(Number(y), Number(m) - 1, Number(d));
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    function formatDateIso(dateObj) {
        const y = dateObj.getFullYear();
        const m = String(dateObj.getMonth() + 1).padStart(2, '0');
        const d = String(dateObj.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    function showToast(message, isError = false) {
        const toast = document.createElement('div');
        toast.className = 'toast' + (isError ? ' toast-danger' : '');
        toast.textContent = message;
        elements.toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.25s ease';
            setTimeout(() => toast.remove(), 250);
        }, 3000);
    }

    // Subtle ambient spotlight cursor tracking (60fps passive requestAnimationFrame)
    (function initAmbientSpotlight() {
        let ticking = false;
        window.addEventListener('mousemove', (e) => {
            if (!ticking) {
                window.requestAnimationFrame(() => {
                    document.documentElement.style.setProperty('--mouse-x', e.clientX + 'px');
                    document.documentElement.style.setProperty('--mouse-y', e.clientY + 'px');
                    ticking = false;
                });
                ticking = true;
            }
        }, { passive: true });
    })();

})();
