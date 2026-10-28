import InquiryList from './InquiryList.js';
import InquiryDetail from './InquiryDetail.js';
import InquiryForm from './InquiryForm.js';
import InquiryReply from './InquiryReply.js';

const { createApp } = window.Vue;
const { createPinia } = window.Pinia;

const { createRouter, createWebHashHistory } = window.VueRouter;

const routes = [
    {
        path: '/',
        component: InquiryList
    },
    {
        path: '/write',
        component: InquiryForm,
        props: {
            mode: 'create'
        }
    },
    {
        path: '/edit/:id',
        component: InquiryForm,
        props: route => ({
            mode: 'edit',
            inquiryId: Number(route.params.id)
        })
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

const app = createApp({});

app.use(createPinia());
app.use(router);

app.component('inquiry-detail', InquiryDetail);
app.component('inquiry-reply', InquiryReply);
app.component('inquiry-form', InquiryForm);

app.mount('#vue-app');