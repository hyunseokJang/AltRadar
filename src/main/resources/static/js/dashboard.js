let priceChart;
let volumeChart;

function loadDashboardData() {
    fetch('/api/market-analysis') // 더 이상 하드코딩된 티커 전달 X
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                updateTable(data.data);
                updateCharts(data.data);
            }
        })
        .catch(error => console.error('Error fetching dashboard data:', error));
}

function updateTable(markets) {
    const tbody = document.querySelector('#marketTable tbody');
    tbody.innerHTML = '';
    markets.forEach(market => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${market.symbol}</td>
            <td>${formatNumber(market.price)}</td>
            <td>${market.ma5 ? formatNumber(market.ma5) : '-'}</td>
            <td>${market.rsi ? market.rsi.toFixed(2) : '-'}</td>
            <td>${market.pumpScore ? market.pumpScore.toFixed(0) : '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateCharts(markets) {
    const labels = markets.map(m => m.symbol);
    const prices = markets.map(m => m.price);
    const volumes = markets.map(m => m.volumeSpike ? 1 : 0);

    if (!priceChart) {
        const ctxPrice = document.getElementById('priceChart').getContext('2d');
        priceChart = new Chart(ctxPrice, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Price (KRW)',
                    data: prices,
                    backgroundColor: 'rgba(54, 162, 235, 0.6)'
                }]
            }
        });
    } else {
        priceChart.data.labels = labels;
        priceChart.data.datasets[0].data = prices;
        priceChart.update();
    }

    if (!volumeChart) {
        const ctxVolume = document.getElementById('volumeChart').getContext('2d');
        volumeChart = new Chart(ctxVolume, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Volume Spike',
                    data: volumes,
                    backgroundColor: 'rgba(255, 99, 132, 0.6)'
                }]
            }
        });
    } else {
        volumeChart.data.labels = labels;
        volumeChart.data.datasets[0].data = volumes;
        volumeChart.update();
    }
}

function formatNumber(num) {
    return num.toLocaleString('en-US', { maximumFractionDigits: 2 });
}