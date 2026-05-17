export default function MessageBanner({ type = 'success', message }) {
  if (!message) return null;

  return <div className={`banner ${type}`}>{message}</div>;
}
