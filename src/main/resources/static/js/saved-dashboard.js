let savedPriceChart;
let savedVolumeChart;

let savedDataCache = []; // 전체 데이터 캐시

function loadSavedData() {
    fetch('/api/crypto/all?limit=100')
        .then(response => response.json())
        .then(data => {
            savedDataCache = data; // 캐시에 저장
            updateSavedTable(data);
            updateSavedCharts(data);
        })
        .catch(error => console.error('Error fetching saved dashboard data:', error));
}

function filterBuyCandidates() {
    // 최근 7일 평균 거래량 대비 2배 이상인지 판단 (여기서는 단순히 Volume 24h > Market Cap*0.02 가정)
    const candidates = savedDataCache.filter(m => 
        m.pumpScore >= 80 &&
        m.trend === 'BULLISH' &&
        m.volume24h > (m.marketCap * 0.02) // 예시 조건
    );
    updateSavedTable(candidates);
    updateSavedCharts(candidates);
}

function updateSavedTable(markets) {
    const tbody = document.querySelector('#savedTable tbody');
    tbody.innerHTML = '';
    markets.forEach(market => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${market.symbol}</td>
            <td>${formatNumber(market.currentPrice)}</td>
            <td>${formatNumber(market.marketCap)}</td>
            <td>${formatNumber(market.volume24h)}</td>
            <td>${market.pumpScore ? market.pumpScore.toFixed(0) : '-'}</td>
            <td>${market.trend || '-'}</td>
            <td>${market.riskLevel || '-'}</td>
            <td>${market.lastUpdated || '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateSavedCharts(markets) {
    const labels = markets.map(m => m.symbol);
    const prices = markets.map(m => m.currentPrice || 0);
    const volumes = markets.map(m => m.volume24h || 0);

    if (!savedPriceChart) {
        const ctxPrice = document.getElementById('savedPriceChart').getContext('2d');
        savedPriceChart = new Chart(ctxPrice, {
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
        savedPriceChart.data.labels = labels;
        savedPriceChart.data.datasets[0].data = prices;
        savedPriceChart.update();
    }

    if (!savedVolumeChart) {
        const ctxVolume = document.getElementById('savedVolumeChart').getContext('2d');
        savedVolumeChart = new Chart(ctxVolume, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Volume 24h',
                    data: volumes,
                    backgroundColor: 'rgba(255, 99, 132, 0.6)'
                }]
            }
        });
    } else {
        savedVolumeChart.data.labels = labels;
        savedVolumeChart.data.datasets[0].data = volumes;
        savedVolumeChart.update();
    }
}

function formatNumber(num) {
    if (num == null) return '-';
    return Number(num).toLocaleString('en-US', { maximumFractionDigits: 2 });
}