<!-- NetworkGraph.vue -->
<template>
  <!-- <h1>{{ props.data.taskId}}</h1> -->
  <div ref="container" class="graphcontent"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Network } from 'vis-network/standalone'
// props获得父组件传递的数据
const props = defineProps(['data'])
// 获得容器元素
const container = ref(null)
let network = null

// 核心配置：各跳邻居的节点数量配额
const NODE_LIMIT_BY_HOP = {
  1: 30,  // 1跳邻居：30个节点
  2: 30,  // 2跳邻居：20个节点
  3: 30   // 3跳邻居：10个节点
}

// 将父组件传递的数据进行一个转换
const transformData = (data) => {
  // 边界判断：无数据则返回空
  if (!data?.edges?.length) return { nodes: [], edges: [] }

  // 1. 找到核心节点（度最大的节点）
  const degreeMap = calculateNodeDegrees(data.edges)
  const coreNodeId = getMostInfluentialNode(degreeMap)
  if (!coreNodeId) return { nodes: [], edges: [] }

  // 2. 按跳数收集指定数量的邻居节点
  const { collectedNodes, nodeHopMap } = collectNodesByHop(coreNodeId, data.edges)
  // 补充核心节点到收集列表
  collectedNodes.add(coreNodeId)

  // 3. 提取这些节点之间的所有关联边
  const selectedEdges = extractEdgesBetweenNodes(collectedNodes, data.edges)

  // 4. 构建节点（保留原样式，可选按跳数区分颜色）
  const nodes = Array.from(collectedNodes).map(id => {
    const hop = nodeHopMap.get(id) || 0
    return {
      id: id,
      title: `ID: ${id}, Hop: ${hop}, User_id: ${data.idMapping?.[id] || 'unknown'}`,
      color: getNodeColorByHop(hop), // 按跳数区分颜色（可选）
      size: hop === 0 ? 30 : 20      // 核心节点更大
    }
  })

  // 5. 构建边（保留原样式）
  const edges = selectedEdges.map((e, i) => ({
    id: i,
    from: e.source,
    to: e.target,
    title: `Type: ${e.type}, Create_time: ${e.timestamp?.slice(0, 16) || 'unknown'}`,
    color: getEdgeColor(e.type),
    width: 2,
    arrows: 'to',
    smooth: { type: 'continuous' }
  }))

  // 日志验证
  console.log(`收集结果：核心节点${coreNodeId}`)
  console.log(`1跳邻居数：${Array.from(collectedNodes).filter(id => nodeHopMap.get(id) === 1).length}`)
  console.log(`2跳邻居数：${Array.from(collectedNodes).filter(id => nodeHopMap.get(id) === 2).length}`)
  console.log(`3跳邻居数：${Array.from(collectedNodes).filter(id => nodeHopMap.get(id) === 3).length}`)
  console.log(`最终节点数：${nodes.length}，最终边数：${edges.length}`)

  return { nodes, edges }
}

// 计算节点度（入度+出度）
const calculateNodeDegrees = (edges) => {
  const degreeMap = new Map()
  edges.forEach(e => {
    degreeMap.set(e.source, (degreeMap.get(e.source) || 0) + 1)
    degreeMap.set(e.target, (degreeMap.get(e.target) || 0) + 1)
  })
  return degreeMap
}

// 找度最大的核心节点
const getMostInfluentialNode = (degreeMap) => {
  let maxDegree = -1, coreNode = null
  for (const [id, degree] of degreeMap.entries()) {
    if (degree > maxDegree) {
      maxDegree = degree
      coreNode = id
    }
  }
  return coreNode
}

// 核心逻辑：按跳数收集指定数量的邻居节点
const collectNodesByHop = (coreNodeId, allEdges) => {
  // 1. 构建邻接表（快速查节点关联边）
  const adjacencyList = buildAdjacencyList(allEdges)
  // 2. 辅助变量
  const nodeHopMap = new Map([[coreNodeId, 0]]) // 核心节点=0跳
  const collectedNodes = new Set()               // 最终收集的节点
  const visitedNodes = new Set([coreNodeId])     // 已访问的节点（避免重复）
  let currentHopQueue = [coreNodeId]             // 当前跳的待遍历节点队列

  // 3. 分层收集节点（1跳→2跳→3跳）
  for (let hop = 1; hop <= 3; hop++) {
    const hopNodeLimit = NODE_LIMIT_BY_HOP[hop] // 当前跳的节点配额
    let collectedInHop = 0                      // 已收集的当前跳节点数
    const nextHopQueue = []                     // 下一跳的待遍历节点

    // 遍历当前跳的所有节点
    while (currentHopQueue.length > 0 && collectedInHop < hopNodeLimit) {
      const currentNode = currentHopQueue.shift()
      const neighborEdges = adjacencyList.get(currentNode) || []

      // 遍历当前节点的邻居
      for (const edge of neighborEdges) {
        // 已收集够当前跳节点，停止
        if (collectedInHop >= hopNodeLimit) break

        // 找到邻居节点
        const neighborNode = edge.source === currentNode ? edge.target : edge.source
        // 未访问过的节点 → 属于当前跳
        if (!visitedNodes.has(neighborNode)) {
          visitedNodes.add(neighborNode)
          nodeHopMap.set(neighborNode, hop) // 标记跳数
          collectedNodes.add(neighborNode)  // 加入收集列表
          nextHopQueue.push(neighborNode)   // 加入下一跳遍历队列
          collectedInHop++                  // 当前跳收集数+1
        }
      }
    }

    // 更新下一跳遍历队列
    currentHopQueue = nextHopQueue
  }

  return { collectedNodes, nodeHopMap }
}

// 提取指定节点集合之间的所有关联边
const extractEdgesBetweenNodes = (nodeSet, allEdges) => {
  return allEdges.filter(edge => {
    // 边的两端都在收集的节点集合中 → 保留这条边
    return nodeSet.has(edge.source) && nodeSet.has(edge.target)
  })
}

// 构建邻接表
const buildAdjacencyList = (edges) => {
  const adj = new Map()
  edges.forEach(e => {
    if (!adj.has(e.source)) adj.set(e.source, [])
    adj.get(e.source).push(e)
    if (!adj.has(e.target)) adj.set(e.target, [])
    adj.get(e.target).push(e)
  })
  return adj
}

// 按跳数给节点配色（可选，便于可视化区分）
const getNodeColorByHop = (hop) => {
  const colorMap = {
    0: '#e74c3c', // 核心节点：红色
    1: '#27ae60', // 1跳邻居：绿色
    2: '#f39c12', // 2跳邻居：橙色
    3: '#3498db'  // 3跳邻居：蓝色
  }
  return colorMap[hop] || '#3498db'
}

// 边颜色映射（保留原逻辑）
const getEdgeColor = (type) => {
  const colors = {
    repost: '#27ae60',   // 转发 - 绿色
    comment: '#e67e22',  // 评论 - 橙色
    thumb: '#9b59b6',    // 点赞 - 紫色
    original: '#3498db'  // 原创 - 蓝色
  }
  return colors[type] || '#95a5a6' // 默认灰色
}

// 绘图逻辑（完全保留原逻辑）
const draw = () => {
  const { nodes, edges } = transformData(props.data)
  
  if (!network) {
    network = new Network(container.value, { nodes, edges }, {
      nodes: { 
        shape: 'dot', 
        size: 25, 
        font: { size: 14 } 
      },
      edges: { 
        arrows: 'to', 
        smooth: true 
      },
      physics: { 
        enabled: true,
        barnesHut: {
          gravitationalConstant: -3000,
          centralGravity: 0.3,
          springLength: 100,
          springConstant: 0.04,
          damping: 0.09,
          avoidOverlap: 0.5
        },
        stabilization: {
          enabled: true,
          iterations: 200,
          updateInterval: 25
        }
      },
      layout: {
        randomSeed: 2
      },
      interaction: {
        hover: true,
        tooltipDelay: 200,
        zoomView: true,
        dragView: true
      }
    })
    
    network.once('stabilizationIterationsDone', () => {
      network.fit({
        animation: {
          duration: 1000,
          easingFunction: 'easeInOutQuad'
        }
      })
    })
    
  } else {
    network.setData({ nodes, edges })
    network.fit()
  }
}

// 监听数据变化+挂载绘图（保留原逻辑）
watch(() => props.data, draw, { deep: true })
onMounted(() => {
  if (container.value) {
    draw()
  }
})
onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>
<style scoped>
  .graphcontent{
    width: 100%;
    height: 400px;
    /* 浅灰背景 */
    background: #f8fafc;
    border-radius: 12px;
  }
  .graphcontent::before{
    content: '';
    position: absolute;
    inset: 0;
    background-size: 50px 50px;
    animation: gridMove 20s linear infinite;
    pointer-events: none;
  }
</style>
